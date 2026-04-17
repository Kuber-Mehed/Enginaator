<template>
  <div class="guest-view min-vh-100 d-flex align-items-center justify-content-center px-3 py-4">
    <div class="guest-shell w-100">
      <div class="room-badge rounded-pill px-4 py-3 text-center mx-auto mb-4">
        <span class="room-badge-label">Room</span>
        <span class="room-badge-number">{{ room }}</span>
      </div>

      <section class="guest-card card border-0 rounded-5 mx-auto">
        <div class="card-body guest-card-body text-center">
          <div
              class="mic-circle rounded-circle d-inline-flex align-items-center justify-content-center mb-4"
              :class="{
              'mic-circle--listening': isListening,
              'mic-circle--processing': isUploading || isSubmittingManual
            }"
          >
            <i :class="micIconClass"></i>
          </div>

          <h1 class="guest-title mb-3">How can we help?</h1>
          <p class="guest-subtitle mb-4">
            Tap “Speak Request” or type your request below
          </p>

          <div v-if="hasError" class="status-alert status-alert--error rounded-4 mb-4">
            {{ activeErrorMessage }}
          </div>

          <div
              v-if="popupMessage"
              class="status-alert rounded-4 mb-4"
              :class="popupAlertCustomClass"
          >
            <div class="fw-semibold mb-1">{{ popupMessage.title }}</div>
            <div>{{ popupMessage.text }}</div>
          </div>

          <div
              v-if="liveTranscript || lockedVoiceText || backendTranscript"
              class="content-panel rounded-4 p-3 p-md-4 mb-4 text-start"
          >
            <div v-if="liveTranscript" class="mb-3">
              <div class="panel-label">Listening</div>
              <div class="panel-text">{{ liveTranscript }}</div>
            </div>

            <div v-if="lockedVoiceText" class="mb-3">
              <div class="panel-label">Your request</div>
              <div class="panel-text">{{ lockedVoiceText }}</div>
            </div>

            <div v-if="backendTranscript">
              <div class="panel-label">Sent to staff</div>
              <div class="panel-text panel-text--highlight">{{ backendTranscript }}</div>
            </div>
          </div>

          <div
              v-if="currentRequest"
              class="content-panel rounded-4 p-3 p-md-4 mb-4 text-start"
          >
            <div class="panel-label">Current request</div>
            <div class="panel-text mb-3">{{ currentRequest.displayText }}</div>

            <div class="d-flex flex-wrap align-items-center gap-2">
              <span class="text-secondary-emphasis small">Status:</span>
              <span
                  class="badge rounded-pill px-3 py-2 fs-6"
                  :class="getStatusBadgeClass(currentRequest.status)"
              >
                {{ getStatusLabel(currentRequest.status) }}
              </span>
            </div>

            <div v-if="currentRequest.backendMessage" class="panel-secondary-text mt-3">
              {{ currentRequest.backendMessage }}
            </div>
          </div>

          <div
              v-if="aiResponseMessage"
              class="content-panel rounded-4 p-3 p-md-4 mb-4 text-start"
          >
            <div class="panel-label">Update</div>
            <div class="panel-text panel-text--highlight">{{ aiResponseMessage }}</div>
          </div>

          <div class="manual-text-block mb-4">
            <textarea
                v-model.trim="manualText"
                class="form-control guest-textarea rounded-4"
                rows="3"
                placeholder="Type your request here..."
                :disabled="isListening || isUploading || isSubmittingManual"
            />
          </div>

          <div class="button-group d-flex flex-wrap gap-3 justify-content-center mb-4">
            <button
                type="button"
                class="btn guest-btn guest-btn-primary rounded-4"
                :disabled="isListening || isUploading || isSubmittingManual"
                @click="startVoiceCapture"
            >
              {{ isListening ? 'Listening...' : 'Speak Request' }}
            </button>

            <button
                type="button"
                class="btn guest-btn guest-btn-secondary rounded-4"
                :disabled="!canStopListening"
                @click="stopVoiceCapture"
            >
              Finish Speaking
            </button>

            <button
                type="button"
                class="btn guest-btn guest-btn-secondary rounded-4"
                :disabled="!canSubmitManualText"
                @click="submitManualRequest"
            >
              {{ isSubmittingManual ? 'Sending...' : 'Send Request' }}
            </button>

            <button
                type="button"
                class="btn guest-btn guest-btn-secondary rounded-4"
                :disabled="isUploading || isListening"
                @click="clearVoiceState"
            >
              Clear
            </button>
          </div>

          <p class="helper-text mb-0">
            You can speak naturally or type your request at any time.
          </p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import api from '@/services/api.ts'
import {createServiceRequest} from "@/services/service-request-service.ts";

interface Props {
  room: string
}

type RequestStatus = 'RECEIVED' | 'IN_PROGRESS' | 'DELIVERED' | 'REJECTED'

interface ActiveRequest {
  id: string
  roomNumber: string
  displayText: string
  status: RequestStatus
  createdAt: string
  backendMessage?: string
}

interface PopupMessage {
  title: string
  text: string
  type: 'success' | 'danger' | 'warning' | 'info'
}

const props = defineProps<Props>()

const AUTO_STOP_DELAY_MS = 2200

const recognition = ref<any>(null)
const mediaRecorder = ref<MediaRecorder | null>(null)
const recordedAudioChunks = ref<Blob[]>([])
const recordedAudioBlob = ref<Blob | null>(null)
const currentStream = ref<MediaStream | null>(null)
const finalVoiceSegments = ref<string[]>([])
const shouldSubmitVoiceOnEnd = ref(false)
const hasSpokenDuringSession = ref(false)
const isStartingVoiceCapture = ref(false)
const silenceTimeoutId = ref<number | null>(null)

const isListening = ref(false)
const isUploading = ref(false)
const isSubmittingManual = ref(false)

const liveTranscript = ref('')
const lockedVoiceText = ref('')
const backendTranscript = ref('')
const manualText = ref('')

const microphoneError = ref<string | null>(null)
const apiError = ref<string | null>(null)
const popupMessage = ref<PopupMessage | null>(null)
const aiResponseMessage = ref<string | null>(null)
const currentRequest = ref<ActiveRequest | null>(null)

onMounted(() => {
  initializeSpeechRecognition()
})

onBeforeUnmount(() => {
  clearSilenceTimeout()

  if (recognition.value) {
    recognition.value.onstart = null
    recognition.value.onresult = null
    recognition.value.onerror = null
    recognition.value.onend = null

    try {
      recognition.value.abort()
    } catch {
      // noop
    }
  }

  stopAudioRecordingOnly()
})

function initializeSpeechRecognition(): void {
  const SpeechRecognition =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition

  if (!SpeechRecognition) {
    microphoneError.value = 'Voice requests are not supported in this browser.'
    return
  }

  const speechRecognition = new SpeechRecognition()
  speechRecognition.continuous = true
  speechRecognition.interimResults = true
  speechRecognition.lang = 'en-US'

  speechRecognition.onstart = () => {
    isListening.value = true
    isStartingVoiceCapture.value = false
    microphoneError.value = null
    apiError.value = null
    popupMessage.value = null
    aiResponseMessage.value = null
    liveTranscript.value = ''
    lockedVoiceText.value = ''
    backendTranscript.value = ''
    finalVoiceSegments.value = []
    hasSpokenDuringSession.value = false
    clearSilenceTimeout()
  }

  speechRecognition.onresult = (event: any) => {
    let interimText = ''

    for (let i = event.resultIndex; i < event.results.length; i += 1) {
      const piece = event.results[i][0].transcript.trim()

      if (!piece) {
        continue
      }

      hasSpokenDuringSession.value = true

      if (event.results[i].isFinal) {
        finalVoiceSegments.value.push(piece)
      } else {
        interimText += `${piece} `
      }
    }

    const finalText = finalVoiceSegments.value.join(' ').trim()
    liveTranscript.value = `${finalText} ${interimText}`.trim()
    lockedVoiceText.value = finalText || liveTranscript.value.trim()

    if (hasSpokenDuringSession.value) {
      restartSilenceTimeout()
    }
  }

  speechRecognition.onerror = (event: any) => {
    isListening.value = false
    isStartingVoiceCapture.value = false
    clearSilenceTimeout()

    if (event.error === 'aborted') {
      return
    }

    shouldSubmitVoiceOnEnd.value = false

    switch (event.error) {
      case 'no-speech':
        microphoneError.value = 'We could not hear your request. Please try again.'
        break
      case 'not-allowed':
        microphoneError.value = 'Microphone access is turned off. Please allow microphone access.'
        break
      case 'network':
        microphoneError.value = 'There was a connection problem. Please try again.'
        break
      default:
        microphoneError.value = 'We could not capture your request. Please try again.'
        break
    }
  }

  speechRecognition.onend = async () => {
    isListening.value = false
    isStartingVoiceCapture.value = false
    clearSilenceTimeout()

    if (!shouldSubmitVoiceOnEnd.value) {
      stopAudioRecordingOnly()
      return
    }

    if (!hasSpokenDuringSession.value && !lockedVoiceText.value.trim() && !liveTranscript.value.trim()) {
      shouldSubmitVoiceOnEnd.value = false
      stopAudioRecordingOnly()
      microphoneError.value = 'We did not catch a request. Please try again.'
      return
    }

    shouldSubmitVoiceOnEnd.value = false
    await finalizeVoiceCaptureAndSend()
  }

  recognition.value = speechRecognition
}

function clearSilenceTimeout(): void {
  if (silenceTimeoutId.value !== null) {
    window.clearTimeout(silenceTimeoutId.value)
    silenceTimeoutId.value = null
  }
}

function restartSilenceTimeout(): void {
  clearSilenceTimeout()
  silenceTimeoutId.value = window.setTimeout(() => {
    if (isListening.value) {
      stopVoiceCapture()
    }
  }, AUTO_STOP_DELAY_MS)
}

async function startVoiceCapture(): Promise<void> {
  if (!recognition.value) {
    microphoneError.value = 'Voice requests are not available in this browser.'
    return
  }

  if (isStartingVoiceCapture.value || isListening.value) {
    return
  }

  if (!props.room.trim()) {
    apiError.value = 'Room number is required.'
    return
  }

  isStartingVoiceCapture.value = true

  try {
    clearMessagesOnly()
    manualText.value = ''
    liveTranscript.value = ''
    lockedVoiceText.value = ''
    backendTranscript.value = ''
    finalVoiceSegments.value = []
    recordedAudioChunks.value = []
    recordedAudioBlob.value = null
    hasSpokenDuringSession.value = false
    shouldSubmitVoiceOnEnd.value = true
    clearSilenceTimeout()

    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    currentStream.value = stream

    const mimeType = MediaRecorder.isTypeSupported('audio/webm;codecs=opus')
        ? 'audio/webm;codecs=opus'
        : MediaRecorder.isTypeSupported('audio/webm')
            ? 'audio/webm'
            : ''

    const recorder = mimeType ? new MediaRecorder(stream, { mimeType }) : new MediaRecorder(stream)
    mediaRecorder.value = recorder

    recorder.ondataavailable = (event: BlobEvent) => {
      if (event.data.size > 0) {
        recordedAudioChunks.value.push(event.data)
      }
    }

    recorder.start(250)
    recognition.value.start()
  } catch {
    shouldSubmitVoiceOnEnd.value = false
    isStartingVoiceCapture.value = false
    microphoneError.value = 'Could not access the microphone. Please check browser permissions.'
    stopAudioRecordingOnly()
  }
}

function stopVoiceCapture(): void {
  clearSilenceTimeout()

  if (!recognition.value || (!isListening.value && !isStartingVoiceCapture.value)) {
    stopAudioRecordingOnly()
    return
  }

  try {
    recognition.value.stop()
  } catch {
    stopAudioRecordingOnly()
  }
}

function stopAudioRecordingOnly(): void {
  if (mediaRecorder.value && mediaRecorder.value.state !== 'inactive') {
    mediaRecorder.value.stop()
  }

  if (currentStream.value) {
    currentStream.value.getTracks().forEach((track) => track.stop())
    currentStream.value = null
  }
}

function waitForRecorderToStop(): Promise<void> {
  return new Promise((resolve) => {
    const recorder = mediaRecorder.value

    if (!recorder || recorder.state === 'inactive') {
      resolve()
      return
    }

    const previousOnStop = recorder.onstop

    recorder.onstop = (event: Event) => {
      if (typeof previousOnStop === 'function') {
        previousOnStop.call(recorder, event)
      }
      resolve()
    }

    recorder.stop()
  })
}

async function finalizeVoiceCaptureAndSend(): Promise<void> {
  if (!lockedVoiceText.value.trim() && !liveTranscript.value.trim()) {
    stopAudioRecordingOnly()
    return
  }

  if (!lockedVoiceText.value.trim()) {
    lockedVoiceText.value = liveTranscript.value.trim()
  }

  await waitForRecorderToStop()

  if (recordedAudioChunks.value.length > 0) {
    recordedAudioBlob.value = new Blob(recordedAudioChunks.value, {
      type: mediaRecorder.value?.mimeType || 'audio/webm',
    })
  }

  if (currentStream.value) {
    currentStream.value.getTracks().forEach((track) => track.stop())
    currentStream.value = null
  }

  await submitVoiceRequest()
}

async function submitVoiceRequest(): Promise<void> {
  if (!lockedVoiceText.value.trim()) {
    return
  }

  if (!recordedAudioBlob.value) {
    apiError.value = 'Audio recording is missing. Please try again.'
    return
  }

  isUploading.value = true
  apiError.value = null

  try {
    const formData = new FormData()
    formData.append('file', recordedAudioBlob.value, 'guest-request.webm')

    await createServiceRequest(props.room, recordedAudioBlob.value)

    backendTranscript.value = lockedVoiceText.value
    aiResponseMessage.value = 'Your request is on its way to the front desk.'

    currentRequest.value = {
      id: `${props.room}-${Date.now()}`,
      roomNumber: props.room,
      displayText: lockedVoiceText.value,
      status: 'RECEIVED',
      createdAt: new Date().toISOString(),
      backendMessage: 'The team has received your request.',
    }

    popupMessage.value = {
      title: 'Request sent',
      text: 'Your voice request has been shared with the hotel staff.',
      type: 'success',
    }
  } catch (error: any) {
    apiError.value =
        error?.response?.data?.message ||
        error?.message ||
        'We could not send your voice request. Please try again.'
  } finally {
    isUploading.value = false
    recordedAudioChunks.value = []
    recordedAudioBlob.value = null
    mediaRecorder.value = null
  }
}

async function submitManualRequest(): Promise<void> {
  if (!manualText.value.trim()) {
    return
  }

  if (!props.room.trim()) {
    apiError.value = 'Room number is required.'
    return
  }

  isSubmittingManual.value = true
  apiError.value = null
  popupMessage.value = null
  aiResponseMessage.value = null

  try {
    const requestText = manualText.value.trim()
    const manualRequestBlob = new Blob([requestText], { type: 'text/plain' })
    const formData = new FormData()
    formData.append('file', manualRequestBlob, 'guest-request.txt')
    await postGuestRequestTxt(props.room, recordedAudioBlob.value)
    await api.post(`/room/${props.room}`, formData)

    backendTranscript.value = requestText
    currentRequest.value = {
      id: `${props.room}-${Date.now()}`,
      roomNumber: props.room,
      displayText: requestText,
      status: 'RECEIVED',
      createdAt: new Date().toISOString(),
      backendMessage: 'The team has received your request.',
    }

    popupMessage.value = {
      title: 'Request sent',
      text: 'Your request has been shared with the hotel staff.',
      type: 'success',
    }

    aiResponseMessage.value = 'Your request is on its way to the front desk.'
    manualText.value = ''
    liveTranscript.value = ''
    lockedVoiceText.value = ''
  } catch (error: any) {
    apiError.value =
        error?.response?.data?.message ||
        error?.message ||
        'We could not send your request. Please try again.'
  } finally {
    isSubmittingManual.value = false
  }
}

function clearMessagesOnly(): void {
  microphoneError.value = null
  apiError.value = null
  popupMessage.value = null
}

function clearVoiceState(): void {
  shouldSubmitVoiceOnEnd.value = false
  clearSilenceTimeout()
  liveTranscript.value = ''
  lockedVoiceText.value = ''
  backendTranscript.value = ''
  recordedAudioBlob.value = null
  recordedAudioChunks.value = []
  finalVoiceSegments.value = []
  hasSpokenDuringSession.value = false
  clearMessagesOnly()

  if (recognition.value && (isListening.value || isStartingVoiceCapture.value)) {
    recognition.value.abort()
  }

  stopAudioRecordingOnly()
  isListening.value = false
  isStartingVoiceCapture.value = false
  mediaRecorder.value = null
}

function getStatusLabel(status: string): string {
  const labels: Record<string, string> = {
    RECEIVED: 'Received',
    IN_PROGRESS: 'In Progress',
    DELIVERED: 'Delivered',
    REJECTED: 'Unavailable',
  }

  return labels[status] || status
}

function getStatusBadgeClass(status: string): string {
  const classes: Record<string, string> = {
    RECEIVED: 'text-bg-secondary',
    IN_PROGRESS: 'text-bg-primary',
    DELIVERED: 'text-bg-success',
    REJECTED: 'text-bg-danger',
  }

  return classes[status] || 'text-bg-secondary'
}

const hasError = computed(() => Boolean(microphoneError.value || apiError.value))
const activeErrorMessage = computed(() => microphoneError.value || apiError.value || '')
const canStopListening = computed(() => isListening.value || isStartingVoiceCapture.value)

const canSubmitManualText = computed(() => {
  return (
      manualText.value.trim().length > 0 &&
      !isSubmittingManual.value &&
      !isListening.value &&
      !isUploading.value &&
      !isStartingVoiceCapture.value
  )
})

const micIconClass = computed(() => {
  if (isUploading.value || isSubmittingManual.value) {
    return 'bi bi-arrow-repeat fs-1'
  }

  if (isListening.value) {
    return 'bi bi-mic-fill fs-1'
  }

  return 'bi bi-mic-mute fs-1'
})

const popupAlertCustomClass = computed(() => {
  switch (popupMessage.value?.type) {
    case 'success':
      return 'status-alert--success'
    case 'danger':
      return 'status-alert--error'
    case 'warning':
      return 'status-alert--warning'
    default:
      return 'status-alert--info'
  }
})
</script>

<style scoped>
.guest-view {
  background:
      radial-gradient(circle at top, rgba(37, 99, 235, 0.1), transparent 30%),
      linear-gradient(90deg, #071225 0%, #020817 50%, #071225 100%);
  color: var(--text-main);
}

.guest-shell {
  max-width: 980px;
}

.room-badge {
  width: fit-content;
  min-width: 120px;
  background: rgba(30, 41, 59, 0.9);
  border: 1px solid rgba(148, 163, 184, 0.16);
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.18);
}

.room-badge-label {
  color: #94a3b8;
  font-size: 0.95rem;
  margin-right: 0.45rem;
}

.room-badge-number {
  color: #f8fafc;
  font-size: 2rem;
  font-weight: 700;
  line-height: 1;
}

.guest-card {
  max-width: 760px;
  background: rgba(30, 41, 59, 0.82);
  border: 1px solid rgba(148, 163, 184, 0.12);
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.28);
  backdrop-filter: blur(12px);
}

.guest-card-body {
  padding: 3.5rem 3rem;
}

.mic-circle {
  width: 140px;
  height: 140px;
  background: rgba(71, 85, 105, 0.38);
  color: #6b7280;
  transition: transform 0.2s ease, background-color 0.2s ease, color 0.2s ease;
}

.mic-circle i {
  font-size: 4rem;
}

.mic-circle--listening {
  background-color: rgba(59, 130, 246, 0.16);
  color: var(--primary);
  transform: scale(1.04);
}

.mic-circle--processing {
  background-color: rgba(34, 197, 94, 0.14);
  color: var(--success);
}

.guest-title {
  color: #f8fafc;
  font-size: clamp(2.25rem, 4vw, 3.1rem);
  font-weight: 800;
  line-height: 1.15;
}

.guest-subtitle {
  color: #cbd5e1;
  font-size: 1.25rem;
}

.status-alert {
  border: 1px solid;
  padding: 1rem 1.25rem;
  text-align: center;
  font-size: 1.05rem;
}

.status-alert--error {
  background: rgba(127, 29, 29, 0.18);
  border-color: rgba(220, 38, 38, 0.7);
  color: #f87171;
}

.status-alert--success {
  background: rgba(20, 83, 45, 0.18);
  border-color: rgba(34, 197, 94, 0.6);
  color: #86efac;
}

.status-alert--warning {
  background: rgba(120, 53, 15, 0.18);
  border-color: rgba(245, 158, 11, 0.6);
  color: #fcd34d;
}

.status-alert--info {
  background: rgba(30, 64, 175, 0.16);
  border-color: rgba(59, 130, 246, 0.5);
  color: #93c5fd;
}

.content-panel {
  background: rgba(15, 23, 42, 0.55);
  border: 1px solid rgba(148, 163, 184, 0.12);
}

.panel-label {
  color: #94a3b8;
  font-size: 0.8rem;
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.06em;
  margin-bottom: 0.5rem;
}

.panel-text {
  color: #e2e8f0;
  font-size: 1rem;
  line-height: 1.55;
}

.panel-text--highlight {
  color: #f8fafc;
}

.panel-secondary-text {
  color: #cbd5e1;
  font-size: 0.95rem;
}

.manual-text-block {
  max-width: 640px;
  margin-left: auto;
  margin-right: auto;
}

.guest-textarea {
  background: rgba(15, 23, 42, 0.52);
  color: #e2e8f0;
  border: 1px solid rgba(148, 163, 184, 0.16);
  resize: none;
  padding: 1rem 1.15rem;
  min-height: 96px;
}

.guest-textarea::placeholder {
  color: #64748b;
}

.guest-textarea:focus {
  background: rgba(15, 23, 42, 0.65);
  color: #f8fafc;
  border-color: rgba(59, 130, 246, 0.6);
  box-shadow: 0 0 0 0.2rem rgba(59, 130, 246, 0.14);
}

.guest-textarea:disabled {
  opacity: 0.65;
}

.button-group {
  max-width: 640px;
  margin-left: auto;
  margin-right: auto;
}

.guest-btn {
  min-width: 180px;
  padding: 1rem 1.5rem;
  font-weight: 700;
  border: 1px solid transparent;
}

.guest-btn-primary {
  background: rgba(71, 85, 105, 0.52);
  color: #f1f5f9;
  border-color: rgba(148, 163, 184, 0.2);
}

.guest-btn-primary:hover:not(:disabled) {
  background: rgba(71, 85, 105, 0.72);
  color: #ffffff;
}

.guest-btn-secondary {
  background: rgba(71, 85, 105, 0.52);
  color: #f1f5f9;
  border-color: rgba(148, 163, 184, 0.2);
}

.guest-btn-secondary:hover:not(:disabled) {
  background: rgba(71, 85, 105, 0.72);
  color: #ffffff;
}

.guest-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.helper-text {
  color: #94a3b8;
  font-size: 1rem;
}

@media (max-width: 768px) {
  .guest-card-body {
    padding: 2rem 1.25rem;
  }

  .mic-circle {
    width: 108px;
    height: 108px;
  }

  .mic-circle i {
    font-size: 3rem;
  }

  .guest-subtitle {
    font-size: 1.05rem;
  }

  .guest-btn {
    width: 100%;
    min-width: 0;
  }

  .button-group {
    flex-direction: column;
  }
}
</style>
