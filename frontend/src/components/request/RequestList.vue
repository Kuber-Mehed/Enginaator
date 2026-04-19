<template>
  <section class="card dashboard-card border-0 shadow-sm rounded-4 requests-panel">
    <div class="card-body p-4 p-xl-4">

      <RequestListEmpty v-if="requests.length === 0" />

      <div v-else class="d-flex flex-column gap-3">
        <RequestListItem
            v-for="request in requests"
            :key="request.id"
            :request="request"
            @click="handleOpenRequest(request)"
        />
      </div>

    </div>
  </section>
</template>

<script setup lang="ts">
import RequestListEmpty from './RequestListEmpty.vue'
import RequestListItem from './RequestListItem.vue'

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

export interface StaffRequest {
  id: string
  roomNumber: string
  text: string
  status: RequestStatus
  createdAt: string
  updatedAt?: string
  staffComment?: string
  requestItems: RequestItemView[]
}

defineProps<{
  requests: StaffRequest[]
}>()

const emit = defineEmits<{
  (e: 'open-request', request: StaffRequest): void
}>()

function handleOpenRequest(request: StaffRequest) {
  emit('open-request', request)
}
</script>

<style scoped>
.dashboard-card {
  background-color: var(--card-bg);
  color: var(--text-main);
}

.requests-panel {
  min-height: 320px;
  overflow: hidden;
}
</style>