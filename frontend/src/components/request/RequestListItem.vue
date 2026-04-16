<template>
  <article class="card border rounded-4 request-item-card">
    <div class="card-body p-4">
      <div class="d-flex flex-column flex-xl-row justify-content-between gap-4">
        <div>
          <div class="fw-bold fs-5 mb-1">Room {{ request.room }}</div>
          <p class="mb-2">{{ request.text }}</p>

          <div class="small text-secondary-emphasis d-flex flex-wrap align-items-center gap-2">
            <span class="badge rounded-pill text-capitalize" :class="statusBadgeClass">
              {{ formattedStatus }}
            </span>
            <span>•</span>
            <span>{{ request.timeAgo }}</span>
          </div>
        </div>

        <div class="d-flex flex-wrap gap-2 align-items-start">
          <button type="button" class="btn btn-sm btn-outline-primary" @click="$emit('set-status', request.id, 'received')">
            Received
          </button>

          <button type="button" class="btn btn-sm btn-outline-warning" @click="$emit('set-status', request.id, 'in_progress')">
            In Progress
          </button>

          <button type="button" class="btn btn-sm btn-outline-success" @click="$emit('set-status', request.id, 'delivered')">
            Delivered
          </button>

          <button type="button" class="btn btn-sm btn-outline-danger" @click="$emit('set-status', request.id, 'rejected')">
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

interface RequestItem {
  id: string
  room: string
  text: string
  status: RequestStatus
  timeAgo: string
}

const props = defineProps<{
  request: RequestItem
}>()

defineEmits<{
  (e: 'set-status', id: string, status: RequestStatus): void
}>()

const formattedStatus = computed(() => props.request.status.replace('_', ' '))

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
</style>