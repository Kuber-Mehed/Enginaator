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

          <h1 class="guest-title mb-3">Say "Hey SVARA"</h1>
          <p class="guest-subtitle mb-4">
            I'm here to help with your room service needs
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

          <!-- Live transcript -->
          <div v-if="liveTranscript || lockedVoiceText || backendTranscript" class="content-panel rounded-4 p-3 p-md-4 mb-4 text-start">
            <div v-if="liveTranscript" class="mb-3">
              <div class="panel-label">Live transcript</div>
              <div class="panel-text">{{ liveTranscript }}</div>
            </div>

            <div v-if="lockedVoiceText" class="mb-3">
              <div class="panel-label">Captured speech</div>
              <div class="panel-text">{{ lockedVoiceText }}</div>
            </div>

            <div v-if="backendTranscript">
              <div class="panel-label">Backend corrected text</div>
              <div class="panel-text panel-text--highlight">{{ backendTranscript }}</div>
            </div>
          </div>

          <!-- Current request -->
          <div v-if="currentRequest" class="content-panel rounded-4 p-3 p-md-4 mb-4 text-start">
            <div class="panel-label">Your current request</div>
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

          <!-- AI response -->
          <div v-if="aiResponseMessage" class="content-panel rounded-4 p-3 p-md-4 mb-4 text-start">
            <div class="panel-label">SVARA response</div>
            <div class="panel-text panel-text--highlight">{{ aiResponseMessage }}</div>
          </div>

          <!-- Manual text area -->
          <div class="manual-text-block mb-4">
            <textarea
                v-model.trim="manualText"
                class="form-control guest-textarea rounded-4"
                rows="3"
                placeholder="Or type your request here..."
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
              {{ isListening ? 'Listening...' : 'Start Listening' }}
            </button>

            <button
                type="button"
                class="btn guest-btn guest-btn-secondary rounded-4"
                :disabled="!canSubmitManualText"
                @click="submitManualRequest"
            >
              {{ isSubmittingManual ? 'Sending...' : 'Send' }}
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
            Voice commands work best in a quiet environment
          </p>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * GuestView
 *
 * Main goals:
 * - show live speech text while the guest is speaking
 * - lock the captured voice text so the guest cannot edit it
 * - send recorded audio file to backend
 * - receive corrected text + AI response + request status from backend
 * - support easier manual text requests
 *
 * Important note:
 * Browser-native speech recognition is used for live transcript.
 * Reliable always-on wake-word detection ("Hey SVARA") usually needs
 * a dedicated wake-word engine. In this version we validate the phrase
 * after recognition result is captured.
 */

import { computed, onMounted, ref } from 'vue'

interface Props {
  room: string
}

type RequestStatus =
    | 'RECEIVED'
    | 'IN_PROGRESS'
    | 'DELIVERED'
    | 'REJECTED'
    | 'CANCELLED'
    | 'APPROVED'
    | 'DECLINED'

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

interface BackendVoiceResponse {
  requestId: string
  correctedText: string
  aiMessage: string
  status: RequestStatus
  accepted: boolean
  backendMessage?: string
}

const props = defineProps<Props>()

/**
 * Browser speech recognition and audio recording states.
 */
const recognition = ref<any>(null)
const mediaRecorder = ref<MediaRecorder | null>(null)
const recordedAudioChunks = ref<Blob[]>([])
const recordedAudioBlob = ref<Blob | null>(null)
const currentStream = ref<MediaStream | null>(null)

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

/**
 * UI states.
 */
const isListening = ref(false)
const isUploading = ref(false)
const isSubmittingManual = ref(false)

/**
 * Voice transcript states.
 *
 * liveTranscript:
 * - updates while guest speaks
 *
 * lockedVoiceText:
 * - final client-side text captured from browser speech recognition
 * - cannot be edited by the guest
 *
 * backendTranscript:
 * - corrected / improved text returned by backend
 */
const liveTranscript = ref('')
const lockedVoiceText = ref('')
const backendTranscript = ref('')

/**
 * Manual typed request text.
 */
const manualText = ref('')

/**
 * Feedback and result states.
 */
const microphoneError = ref<string | null>(null)
const apiError = ref<string | null>(null)
const popupMessage = ref<PopupMessage | null>(null)
const aiResponseMessage = ref<string | null>(null)
const currentRequest = ref<ActiveRequest | null>(null)

/**
 * Wake phrase required before voice request is accepted.
 */
const wakePhrase = 'hey svara'

onMounted(() => {
  initializeSpeechRecognition()
  fetchRoomRequests()
})

/**
 * Initializes browser speech recognition if supported.
 */
function initializeSpeechRecognition(): void {
  const SpeechRecognition =
      (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition

  if (!SpeechRecognition) {
    microphoneError.value =
        'Speech recognition is not supported in this browser.'
    return
  }

  const speechRecognition = new SpeechRecognition()
  speechRecognition.continuous = true
  speechRecognition.interimResults = true
  speechRecognition.lang = 'en-US'

  speechRecognition.onstart = () => {
    isListening.value = true
    microphoneError.value = null
    apiError.value = null
    popupMessage.value = null
    aiResponseMessage.value = null
    liveTranscript.value = ''
    lockedVoiceText.value = ''
    backendTranscript.value = ''
  }

  speechRecognition.onresult = (event: any) => {
    let currentLiveText = ''
    let finalText = ''

    for (let i = event.resultIndex; i < event.results.length; i += 1) {
      const piece = event.results[i][0].transcript

      if (event.results[i].isFinal) {
        finalText += piece
      } else {
        currentLiveText += piece
      }
    }

    liveTranscript.value = `${finalText} ${currentLiveText}`.trim()

    if (finalText.trim()) {
      lockedVoiceText.value = finalText.trim()
    }
  }

  speechRecognition.onerror = (event: any) => {
    isListening.value = false

    switch (event.error) {
      case 'no-speech':
        microphoneError.value = 'No speech detected. Please try again.'
        break
      case 'not-allowed':
        microphoneError.value =
            'Microphone access denied. Please allow microphone access.'
        break
      case 'network':
        microphoneError.value = 'Network error. Please check your connection.'
        break
      default:
        microphoneError.value = `Speech recognition error: ${event.error}`
        break
    }

    stopAudioRecordingOnly()
  }

  speechRecognition.onend = async () => {
    isListening.value = false
    await finalizeVoiceCaptureAndSend()
  }

  recognition.value = speechRecognition
}

/**
 * Starts both:
 * - browser speech recognition for live transcript
 * - MediaRecorder for raw audio file
 */
async function startVoiceCapture(): Promise<void> {
  if (!recognition.value) {
    microphoneError.value =
        'Speech recognition is not available in this browser.'
    return
  }

  try {
    clearMessagesOnly()

    const stream = await navigator.mediaDevices.getUserMedia({ audio: true })
    currentStream.value = stream

    recordedAudioChunks.value = []
    recordedAudioBlob.value = null

    const recorder = new MediaRecorder(stream)
    mediaRecorder.value = recorder

    recorder.ondataavailable = (event: BlobEvent) => {
      if (event.data.size > 0) {
        recordedAudioChunks.value.push(event.data)
      }
    }

    recorder.start()
    recognition.value.start()
  } catch (error) {
    microphoneError.value =
        'Could not access the microphone. Please check browser permissions.'
  }
}

/**
 * Stops recognition manually.
 * Final upload happens in recognition.onend.
 */
function stopVoiceCapture(): void {
  if (recognition.value && isListening.value) {
    recognition.value.stop()
  } else {
    stopAudioRecordingOnly()
  }
}

/**
 * Stops only the audio recorder and media stream.
 */
function stopAudioRecordingOnly(): void {
  if (mediaRecorder.value && mediaRecorder.value.state !== 'inactive') {
    mediaRecorder.value.stop()
  }

  if (currentStream.value) {
    currentStream.value.getTracks().forEach((track) => track.stop())
    currentStream.value = null
  }
}

/**
 * Final step after speech recognition ends:
 * - stop and build audio blob
 * - validate wake phrase
 * - upload voice request to backend
 */
async function finalizeVoiceCaptureAndSend(): Promise<void> {
  stopAudioRecordingOnly()

  if (!lockedVoiceText.value.trim()) {
    return
  }

  if (!startsWithWakePhrase(lockedVoiceText.value)) {
    popupMessage.value = {
      title: 'Wake phrase not detected',
      text: 'Please start your voice request with “Hey SVARA”.',
      type: 'warning',
    }
    return
  }

  if (recordedAudioChunks.value.length > 0) {
    recordedAudioBlob.value = new Blob(recordedAudioChunks.value, {
      type: 'audio/webm',
    })
  }

  await submitVoiceRequest()
}

/**
 * Sends the locked raw client transcript + recorded audio file to backend.
 *
 * Frontend does NOT edit the spoken text.
 * Backend can later return corrected text.
 */
async function submitVoiceRequest(): Promise<void> {
  if (!lockedVoiceText.value.trim()) {
    return
  }

  isUploading.value = true
  apiError.value = null

  try {
    const formData = new FormData()
    formData.append('roomNumber', props.room)
    formData.append('rawTranscript', lockedVoiceText.value)

    if (recordedAudioBlob.value) {
      formData.append(
          'audio',
          recordedAudioBlob.value,
          `room-${props.room}-${Date.now()}.webm`
      )
    }

    const response = await fetch('/api/guest/voice-requests', {
      method: 'POST',
      body: formData,
    })

    if (!response.ok) {
      throw new Error('Failed to submit voice request.')
    }

    const data: BackendVoiceResponse = await response.json()

    backendTranscript.value = data.correctedText
    aiResponseMessage.value = data.aiMessage

    currentRequest.value = {
      id: data.requestId,
      roomNumber: props.room,
      displayText: data.correctedText,
      status: data.status,
      createdAt: new Date().toISOString(),
      backendMessage: data.backendMessage || data.aiMessage,
    }

    popupMessage.value = {
      title: data.accepted ? 'Request sent' : 'Request needs attention',
      text: data.aiMessage,
      type: data.accepted ? 'success' : 'warning',
    }
  } catch (error) {
    apiError.value =
        error instanceof Error
            ? error.message
            : 'An error occurred while sending the voice request.'
  } finally {
    isUploading.value = false
  }
}

/**
 * Manual text request flow.
 * Easier and separate from voice.
 */
async function submitManualRequest(): Promise<void> {
  if (!manualText.value.trim()) {
    return
  }

  isSubmittingManual.value = true
  apiError.value = null
  popupMessage.value = null
  aiResponseMessage.value = null

  try {
    const response = await fetch('/api/guest/requests', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        roomNumber: props.room,
        text: manualText.value,
        source: 'manual',
      }),
    })

    if (!response.ok) {
      throw new Error('Failed to submit manual request.')
    }

    const data = await response.json()

    currentRequest.value = {
      id: data.id,
      roomNumber: props.room,
      displayText: data.text,
      status: data.status,
      createdAt: data.createdAt,
      backendMessage: data.message,
    }

    popupMessage.value = {
      title: 'Request sent',
      text: data.message || 'Your request has been sent successfully.',
      type: 'success',
    }

    manualText.value = ''
  } catch (error) {
    apiError.value =
        error instanceof Error
            ? error.message
            : 'An error occurred while sending the request.'
  } finally {
    isSubmittingManual.value = false
  }
}

/**
 * Fetches latest active room request on page load.
 */
async function fetchRoomRequests(): Promise<void> {
  try {
    const response = await fetch(`/api/guest/requests/${props.room}`)

    if (!response.ok) {
      return
    }

    const requests = await response.json()
    const activeRequest = requests.find(
        (request: any) =>
            !['DELIVERED', 'REJECTED', 'CANCELLED'].includes(request.status)
    )

    if (activeRequest) {
      currentRequest.value = {
        id: activeRequest.id,
        roomNumber: activeRequest.roomNumber,
        displayText: activeRequest.text,
        status: activeRequest.status,
        createdAt: activeRequest.createdAt,
        backendMessage: activeRequest.message,
      }
    }
  } catch (error) {
    console.error('Failed to fetch room requests:', error)
  }
}

/**
 * Clears only temporary feedback messages.
 */
function clearMessagesOnly(): void {
  microphoneError.value = null
  apiError.value = null
  popupMessage.value = null
}

/**
 * Clears voice-only state.
 */
function clearVoiceState(): void {
  liveTranscript.value = ''
  lockedVoiceText.value = ''
  backendTranscript.value = ''
  recordedAudioBlob.value = null
  recordedAudioChunks.value = []
  clearMessagesOnly()

  if (recognition.value && isListening.value) {
    recognition.value.abort()
  }

  stopAudioRecordingOnly()
  isListening.value = false
}

/**
 * Verifies that the guest started with the wake phrase.
 */
function startsWithWakePhrase(text: string): boolean {
  return text.trim().toLowerCase().startsWith(wakePhrase)
}

function getStatusLabel(status: string): string {
  const labels: Record<string, string> = {
    RECEIVED: 'Received',
    IN_PROGRESS: 'In Progress',
    DELIVERED: 'Delivered',
    REJECTED: 'Rejected',
    CANCELLED: 'Cancelled',
    APPROVED: 'Approved',
    DECLINED: 'Declined',
  }

  return labels[status] || status
}

function getStatusBadgeClass(status: string): string {
  const classes: Record<string, string> = {
    RECEIVED: 'text-bg-secondary',
    IN_PROGRESS: 'text-bg-primary',
    DELIVERED: 'text-bg-success',
    REJECTED: 'text-bg-danger',
    CANCELLED: 'text-bg-warning',
    APPROVED: 'text-bg-success',
    DECLINED: 'text-bg-danger',
  }

  return classes[status] || 'text-bg-secondary'
}

const hasError = computed(() => Boolean(microphoneError.value || apiError.value))

const activeErrorMessage = computed(() => {
  return microphoneError.value || apiError.value || ''
})

const canStopListening = computed(() => isListening.value)

const canSubmitManualText = computed(() => {
  return (
      manualText.value.trim().length > 0 &&
      !isSubmittingManual.value &&
      !isListening.value &&
      !isUploading.value
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

const popupAlertClass = computed(() => {
  switch (popupMessage.value?.type) {
    case 'success':
      return 'alert-success'
    case 'danger':
      return 'alert-danger'
    case 'warning':
      return 'alert-warning'
    default:
      return 'alert-info'
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