<template>
  <section class="card dashboard-card border-0 shadow-sm rounded-4 requests-panel">
    <div class="card-body p-4">
      <RequestListEmpty v-if="requests.length === 0" />

      <div v-else class="d-flex flex-column gap-3">
        <RequestListItem
            v-for="request in requests"
            :key="request.id"
            :request="request"
            @set-status="handleSetStatus"
        />
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import RequestListEmpty from './RequestListEmpty.vue'
import RequestListItem from './RequestListItem.vue'

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

defineProps<{
  requests: StaffRequest[]
}>()

const emit = defineEmits<{
  (e: 'set-status', id: string, status: RequestStatus): void
}>()

function handleSetStatus(id: string, status: RequestStatus) {
  emit('set-status', id, status)
}
</script>

<style scoped>
.dashboard-card {
  background-color: var(--card-bg);
  color: var(--text-main);
}

.requests-panel {
  min-height: 320px;
}
</style>