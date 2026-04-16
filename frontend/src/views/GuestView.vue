<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'

interface Props {
  room: string
}

interface Request {
  id: string
  roomNumber: string
  text: string
  status: string
  createdAt: string
}

const props = defineProps<Props>()

const isListening = ref(false)
const transcript = ref('')
const microphoneError = ref<string | null>(null)
const recognition = ref<any>(null)
const currentRequest = ref<Request | null>(null)
const confirmationMessage = ref<string | null>(null)
const isSubmitting = ref(false)
const apiError = ref<string | null>(null)

onMounted(() => {
  // Initialize Web Speech API
  const SpeechRecognition = (window as any).SpeechRecognition || (window as any).webkitSpeechRecognition

  if (SpeechRecognition) {
    recognition.value = new SpeechRecognition()
    recognition.value.continuous = false
    recognition.value.interimResults = true
    recognition.value.lang = 'en-US'

    recognition.value.onstart = () => {
      isListening.value = true
      microphoneError.value = null
      confirmationMessage.value = null
    }

    recognition.value.onresult = (event: any) => {
      let interim = ''
      for (let i = event.resultIndex; i < event.results.length; i++) {
        const transcriptSegment = event.results[i][0].transcript
        if (event.results[i].isFinal) {
          transcript.value = transcriptSegment
        } else {
          interim += transcriptSegment
        }
      }
      if (interim) {
        transcript.value = interim
      }
    }

    recognition.value.onerror = (event: any) => {
      isListening.value = false
      if (event.error === 'no-speech') {
        microphoneError.value = 'No speech detected. Please try again.'
      } else if (event.error === 'not-allowed') {
        microphoneError.value = 'Microphone access denied. Please allow microphone access.'
      } else if (event.error === 'network') {
        microphoneError.value = 'Network error. Please check your connection.'
      } else {
        microphoneError.value = `Error: ${event.error}`
      }
    }

    recognition.value.onend = () => {
      isListening.value = false
      // Auto-submit if we have a transcript
      if (transcript.value.trim()) {
        submitRequest()
      }
    }
  }

  // Fetch existing requests for the room
  fetchRoomRequests()
})

const startListening = () => {
  if (recognition.value) {
    transcript.value = ''
    microphoneError.value = null
    confirmationMessage.value = null
    apiError.value = null
    recognition.value.start()
  }
}

const submitRequest = async () => {
  isSubmitting.value = true
  apiError.value = null

  try {
    const response = await fetch('/api/guest/requests', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        roomNumber: props.room,
        text: transcript.value,
      }),
    })

    if (!response.ok) {
      throw new Error('Failed to submit request')
    }

    const data = await response.json()
    currentRequest.value = data
    confirmationMessage.value = 'Your request has been received!'
    transcript.value = ''
    microphoneError.value = null

    // Hide confirmation after 3 seconds
    setTimeout(() => {
      confirmationMessage.value = null
    }, 3000)
  } catch (error) {
    apiError.value = error instanceof Error ? error.message : 'An error occurred while submitting your request.'
  } finally {
    isSubmitting.value = false
  }
}

const clearTranscript = () => {
  transcript.value = ''
  microphoneError.value = null
  confirmationMessage.value = null
  apiError.value = null
  if (isListening.value && recognition.value) {
    recognition.value.abort()
  }
  isListening.value = false
}

const fetchRoomRequests = async () => {
  try {
    const response = await fetch(`/api/guest/requests/${props.room}`)
    if (response.ok) {
      const requests = await response.json()
      // Get the latest active request
      const activeRequest = requests.find((r: Request) =>
        !['DELIVERED', 'REJECTED', 'CANCELLED'].includes(r.status)
      )
      if (activeRequest) {
        currentRequest.value = activeRequest
      }
    }
  } catch (error) {
    console.error('Failed to fetch room requests:', error)
  }
}

const getStatusLabel = (status: string): string => {
  const labels: Record<string, string> = {
    RECEIVED: 'Received',
    IN_PROGRESS: 'In Progress',
    DELIVERED: 'Delivered',
    REJECTED: 'Rejected',
    CANCELLED: 'Cancelled',
  }
  return labels[status] || status
}

const getStatusColor = (status: string): string => {
  const colors: Record<string, string> = {
    RECEIVED: '#94a3b8',
    IN_PROGRESS: '#3b82f6',
    DELIVERED: '#10b981',
    REJECTED: '#ef4444',
    CANCELLED: '#f97316',
  }
  return colors[status] || '#94a3b8'
}

const hasError = computed(() => microphoneError.value !== null || apiError.value !== null)
const hasTranscript = computed(() => transcript.value.trim().length > 0)
</script>

<template>
  <div class="guest-container">
    <div class="room-badge">
      <span>Room {{ room }}</span>
    </div>

    <div class="voice-interface">
      <div class="microphone-icon">
        <i class="bi bi-mic-mute"></i>
      </div>

      <h1 class="main-heading">Say "Hey SVARA"</h1>
      <p class="subtitle">I'm here to help with your room service needs</p>

      <div v-if="hasError" class="error-message">
        {{ microphoneError || apiError }}
      </div>

      <div v-if="confirmationMessage" class="confirmation-message">
        {{ confirmationMessage }}
      </div>

      <div v-if="hasTranscript" class="transcript-display">
        {{ transcript }}
      </div>

      <div v-if="currentRequest" class="request-status-block">
        <div class="request-header">Your Current Request</div>
        <div class="request-text">{{ currentRequest.text }}</div>
        <div class="request-status">
          <span class="status-label">Status:</span>
          <span class="status-value" :style="{ color: getStatusColor(currentRequest.status) }">
            {{ getStatusLabel(currentRequest.status) }}
          </span>
        </div>
      </div>

      <div class="button-group">
        <button
          @click="startListening"
          :disabled="isListening"
          class="btn btn-listen"
        >
          {{ isListening ? 'Listening...' : 'Start Listening' }}
        </button>
        <button
          @click="clearTranscript"
          class="btn btn-clear"
        >
          Clear
        </button>
      </div>

      <p class="helper-text">Voice commands work best in a quiet environment</p>
    </div>
  </div>
</template>

<style scoped>
.guest-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #0a0e27 0%, #1a1f3a 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 2rem;
  position: relative;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, Cantarell, sans-serif;
}

.room-badge {
  position: absolute;
  top: 2rem;
  left: 50%;
  transform: translateX(-50%);
  z-index: 10;
  background: rgba(255, 255, 255, 0.1);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 2rem;
  padding: 0.75rem 1.5rem;
  color: #b0b9d6;
  font-weight: 500;
  font-size: 1rem;
}

.voice-interface {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  background: rgba(30, 41, 59, 0.6);
  border: 1px solid rgba(148, 163, 184, 0.2);
  border-radius: 2rem;
  padding: 3rem 2rem;
  max-width: 600px;
  width: 100%;
  margin-top: 6rem;
  backdrop-filter: blur(10px);
}

.microphone-icon {
  width: 120px;
  height: 120px;
  background: rgba(71, 85, 105, 0.4);
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 2rem;
  font-size: 3rem;
  color: #94a3b8;
}

.main-heading {
  font-size: 2.5rem;
  font-weight: 700;
  color: #ffffff;
  margin: 1.5rem 0 1rem 0;
  text-align: center;
  line-height: 1.2;
}

.subtitle {
  font-size: 1.125rem;
  color: #cbd5e1;
  margin-bottom: 2rem;
  text-align: center;
}

.error-message {
  background: rgba(220, 38, 38, 0.1);
  border: 1px solid #dc2626;
  border-radius: 0.75rem;
  padding: 1rem 1.5rem;
  color: #fca5a5;
  margin-bottom: 1.5rem;
  width: 100%;
  text-align: center;
  font-size: 0.95rem;
}

.confirmation-message {
  background: rgba(16, 185, 129, 0.1);
  border: 1px solid #10b981;
  border-radius: 0.75rem;
  padding: 1rem 1.5rem;
  color: #86efac;
  margin-bottom: 1.5rem;
  width: 100%;
  text-align: center;
  font-size: 0.95rem;
}

.transcript-display {
  background: rgba(148, 163, 184, 0.1);
  border: 1px solid rgba(148, 163, 184, 0.3);
  border-radius: 0.75rem;
  padding: 1rem 1.5rem;
  color: #e2e8f0;
  margin-bottom: 1.5rem;
  width: 100%;
  text-align: center;
  min-height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-style: italic;
}

.request-status-block {
  background: rgba(59, 130, 246, 0.1);
  border: 1px solid rgba(59, 130, 246, 0.3);
  border-radius: 0.75rem;
  padding: 1.5rem;
  color: #e2e8f0;
  margin-bottom: 1.5rem;
  width: 100%;
}

.request-header {
  font-size: 0.875rem;
  color: #94a3b8;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin-bottom: 0.5rem;
}

.request-text {
  font-size: 1rem;
  color: #ffffff;
  margin-bottom: 1rem;
  font-weight: 500;
}

.request-status {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  font-size: 0.95rem;
}

.status-label {
  color: #94a3b8;
}

.status-value {
  font-weight: 600;
}

.button-group {
  display: flex;
  gap: 1rem;
  width: 100%;
  margin-bottom: 2rem;
  flex-wrap: wrap;
  justify-content: center;
}

.btn {
  flex: 1;
  min-width: 140px;
  padding: 1rem 1.5rem;
  font-size: 1rem;
  font-weight: 600;
  border-radius: 0.75rem;
  border: none;
  cursor: pointer;
  transition: all 0.3s ease;
  text-transform: none;
}

.btn-listen {
  background: rgba(71, 85, 105, 0.6);
  color: #e2e8f0;
  border: 1px solid rgba(148, 163, 184, 0.3);
}

.btn-listen:hover:not(:disabled) {
  background: rgba(71, 85, 105, 0.8);
  border-color: rgba(148, 163, 184, 0.5);
}

.btn-listen:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-clear {
  background: rgba(71, 85, 105, 0.6);
  color: #e2e8f0;
  border: 1px solid rgba(148, 163, 184, 0.3);
}

.btn-clear:hover {
  background: rgba(71, 85, 105, 0.8);
  border-color: rgba(148, 163, 184, 0.5);
}

.helper-text {
  color: #94a3b8;
  font-size: 0.875rem;
  text-align: center;
  margin: 0;
}

@media (max-width: 600px) {
  .guest-container {
    padding: 1rem;
  }

  .voice-interface {
    padding: 2rem 1.5rem;
    border-radius: 1.5rem;
  }

  .main-heading {
    font-size: 1.75rem;
  }

  .microphone-icon {
    width: 100px;
    height: 100px;
    font-size: 2.5rem;
  }

  .button-group {
    flex-direction: column;
  }

  .btn {
    width: 100%;
  }
}
</style>
