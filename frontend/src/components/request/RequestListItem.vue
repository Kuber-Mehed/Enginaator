<template>
  <article class="card border rounded-4 request-item-card h-100">
    <div class="card-body p-4">
      <div class="request-layout d-flex flex-column flex-xl-row justify-content-between gap-4">
        <div class="request-main min-w-0">
          <div class="fw-bold fs-5 mb-1 room-title">
            Room {{ request.roomNumber }}
          </div>
          <p class="mb-2 request-text">{{ request.text }}</p>

          <div
              v-if="request.requestItems.length > 0"
              class="small text-secondary-emphasis d-flex flex-wrap gap-2 mb-2"
          >
            <span
                v-for="item in request.requestItems"
                :key="`${request.id}-${item.itemName}`"
                class="badge rounded-pill request-chip"
            >
              {{ item.quantity }} × {{ item.itemName }}
            </span>
          </div>

          <div class="small text-secondary-emphasis d-flex flex-wrap align-items-center gap-2">
            <span class="badge rounded-pill text-capitalize" :class="statusBadgeClass">
              {{ formattedStatus }}
            </span>
            <span>•</span>
            <div class="time">

              <span>{{ timeAgoLabel }}</span>
            </div>
          </div>
        </div>

        <div class="request-actions d-grid gap-2 align-items-start">
          <button
              type="button"
              class="btn btn-sm btn-outline-primary"
              @click="$emit('set-status', request.id, 'received')"
          >
            Received
          </button>

          <button
              type="button"
              class="btn btn-sm btn-outline-warning"
              @click="$emit('set-status', request.id, 'in_progress')"
          >
            In Progress
          </button>

          <button
              type="button"
              class="btn btn-sm btn-outline-success"
              @click="$emit('set-status', request.id, 'delivered')"
          >
            Delivered
          </button>

          <button
              type="button"
              class="btn btn-sm btn-outline-danger"
              @click="$emit('set-status', request.id, 'rejected')"
          >
            Reject
          </button>
        </div>
      </div>
    </div>
  </article>
</template>

<script setup lang="ts">
import { computed } from 'vue'

type RequestStatus = 'received' | 'in_progress' | 'delivered' | 'rejected'

interface RequestItemView {
  itemName: string
  quantity: number
}

interface StaffRequest {
  id: string
  roomNumber: string
  text: string
  status: RequestStatus
  createdAt: string
  updatedAt?: string
  requestItems: RequestItemView[]
}

const props = defineProps<{
  request: StaffRequest
}>()

defineEmits<{
  (e: 'set-status', id: string, status: RequestStatus): void
}>()

const formattedStatus = computed(() => props.request.status.replace('_', ' '))

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
    case 'received':
      return 'text-bg-primary'
    case 'in_progress':
      return 'text-bg-warning'
    case 'delivered':
      return 'text-bg-success'
    case 'rejected':
      return 'text-bg-danger'
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

.request-text {
  overflow-wrap: anywhere;
}

.request-chip {
  background: rgba(148, 163, 184, 0.14);
  color: var(--text-main);
}

.request-actions {
  grid-template-columns: repeat(2, minmax(120px, 1fr));
  min-width: min(100%, 260px);
}

.room-title, .request-text, .time {
  color: var(--text-main);
}

@media (max-width: 575.98px) {
  .request-actions {
    grid-template-columns: 1fr;
  }
}
</style>
