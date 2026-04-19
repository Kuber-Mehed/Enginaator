<template>
  <article
      class="card border rounded-4 request-item-card h-100 request-item-card--interactive"
      role="button"
      tabindex="0"
      @click="emitOpen"
      @keydown.enter.prevent="emitOpen"
      @keydown.space.prevent="emitOpen"
  >
    <div class="card-body p-4">
      <div class="request-layout d-flex flex-column gap-3">
        <div class="request-main min-w-0">
          <div class="fw-bold fs-5 mb-1 room-title">
            Room {{ request.roomNumber }}
          </div>

          <p class="mb-2 request-text">
            {{ request.text }}
          </p>

          <div
              v-if="request.requestItems.length > 0"
              class="small text-secondary-emphasis d-flex flex-wrap gap-2 mb-2"
          >
            <span
                v-for="item in request.requestItems"
                :key="`${request.id}-${item.itemName}`"
                class="badge rounded-pill request-chip"
            >
              {{ item.quantityRequested }} × {{ item.itemName }}
            </span>
          </div>

          <p
              v-if="request.staffComment?.trim()"
              class="small mb-2 request-comment"
          >
            Staff note: {{ request.staffComment }}
          </p>

          <div class="small text-secondary-emphasis d-flex flex-wrap align-items-center gap-2">
            <span class="badge rounded-pill" :class="statusBadgeClass">
              {{ formattedStatus }}
            </span>
            <span>•</span>
            <span class="time">{{ timeAgoLabel }}</span>
          </div>
        </div>

        <div class="small text-secondary-emphasis">
          Click to manage request
        </div>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type RequestStatus =
    | 'RECEIVED'
    | 'IN_PROGRESS'
    | 'DELIVERED'
    | 'REJECTED'
    | 'CANCELLED'

interface RequestItemView {
  itemName: string
  quantityRequested: number
  quantityFulfilled: number
}

interface StaffRequest {
  id: string
  roomNumber: string
  text: string
  status: RequestStatus
  createdAt: string
  updatedAt?: string
  staffComment?: string
  requestItems: RequestItemView[]
}

const props = defineProps<{
  request: StaffRequest
}>()

const emit = defineEmits<{
  (e: 'open-request', request: StaffRequest): void
}>()

function emitOpen() {
  emit('open-request', props.request)
}

const formattedStatus = computed(() => {
  switch (props.request.status) {
    case 'IN_PROGRESS':
      return 'In Progress'
    case 'DELIVERED':
      return 'Delivered'
    case 'REJECTED':
      return 'Rejected'
    case 'CANCELLED':
      return 'Cancelled'
    case 'RECEIVED':
    default:
      return 'Received'
  }
})

const timeAgoLabel = computed(() => {
  const created = new Date(props.request.createdAt).getTime()

  if (Number.isNaN(created)) {
    return 'just now'
  }

  const diffMs = Date.now() - created
  const diffMin = Math.floor(diffMs / 60000)

  if (diffMin <= 0) {
    return 'just now'
  }

  if (diffMin < 60) {
    return `${diffMin}m ago`
  }

  const diffHours = Math.floor(diffMin / 60)

  if (diffHours < 24) {
    return `${diffHours}h ago`
  }

  const diffDays = Math.floor(diffHours / 24)
  return `${diffDays}d ago`
})

const statusBadgeClass = computed(() => {
  switch (props.request.status) {
    case 'RECEIVED':
      return 'text-bg-primary'
    case 'IN_PROGRESS':
      return 'text-bg-warning'
    case 'DELIVERED':
      return 'text-bg-success'
    case 'REJECTED':
      return 'text-bg-danger'
    case 'CANCELLED':
      return 'text-bg-secondary'
    default:
      return 'text-bg-secondary'
  }
})
</script>

<style scoped>
.request-item-card {
  background-color: transparent;
  border-color: var(--border-color) !important;
}

.request-item-card--interactive {
  cursor: pointer;
  transition: transform 0.16s ease, border-color 0.16s ease, box-shadow 0.16s ease;
}

.request-item-card--interactive:hover {
  transform: translateY(-1px);
  border-color: var(--primary) !important;
}

.request-item-card--interactive:focus-visible {
  outline: 2px solid var(--primary);
  outline-offset: 2px;
}

.request-text {
  overflow-wrap: anywhere;
}

.request-chip {
  background: rgba(148, 163, 184, 0.14);
  color: var(--text-main);
}

.request-comment {
  color: var(--text-muted);
}

.room-title,
.request-text,
.time {
  color: var(--text-main);
}
</style>