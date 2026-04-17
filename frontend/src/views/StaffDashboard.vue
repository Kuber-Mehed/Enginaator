<template>
  <div class="staff-dashboard min-vh-100">
    <div class="container-fluid px-0">
      <div class="row g-0 min-vh-100 align-items-stretch">
        <Sidebar :items="navigationItems" :current-path="route.path">
          <template #bottom>
            <ThemeToggleButton :theme="theme" @toggle="toggleTheme" />
          </template>
        </Sidebar>

        <main class="col main-content position-relative">
          <div class="main-content__inner p-4 p-xl-5">
            <header class="mb-4 mb-xl-5">
              <h1 class="display-5 fw-bold mb-2">Staff Dashboard</h1>
              <p class="text-secondary-emphasis mb-0">
                Monitor and manage room service requests in real-time
              </p>
            </header>

            <section class="row g-4 mb-4">
              <div
                  v-for="card in statsCards"
                  :key="card.key"
                  class="col-12 col-md-6 col-xl-3"
              >
                <StatCard
                    :label="card.label"
                    :value="card.value"
                    :icon="card.icon"
                    :icon-color-class="card.iconColorClass"
                />
              </div>
            </section>

            <section class="mb-4 d-flex flex-column gap-3 dashboard-toolbar">
              <RequestFilters v-model="selectedTab" :tabs="tabs" />

              <div
                  v-if="dashboardMessage"
                  class="alert rounded-4 border-0 mb-0"
                  :class="dashboardMessage.type === 'error' ? 'alert-danger' : 'alert-info'"
              >
                {{ dashboardMessage.text }}
              </div>
            </section>

            <RequestList :requests="visibleRequests" @set-status="setStatus" />
          </div>

          <Transition name="toast-slide">
            <div v-if="activeToast" class="staff-toast shadow-lg rounded-4 p-3">
              <div class="small text-uppercase fw-semibold text-secondary-emphasis mb-1">
                New request
              </div>
              <div class="fw-bold mb-1">Room {{ activeToast.roomNumber }}</div>
              <div class="staff-toast__text">{{ activeToast.text }}</div>
            </div>
          </Transition>

          <audio ref="notificationAudio" preload="auto">
            <source src="../assets/sounds/request-notification.mp3" type="audio/mpeg" />
          </audio>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import Sidebar from '../components/common/Sidebar.vue'
import ThemeToggleButton from '../components/common/ThemeToggleButton.vue'
import StatCard from '../components/common/StatCard.vue'
import RequestFilters from '../components/request/RequestFilters.vue'
import RequestList from '../components/request/RequestList.vue'
import { getServiceRequests } from '@/services/service-request-service.ts'

type RequestStatus = 'received' | 'in_progress' | 'delivered' | 'rejected'
type RequestTab = 'active' | 'all'
type ThemeMode = 'dark' | 'light'
type MessageType = 'info' | 'error'

interface RequestItemView {
  itemName: string
  quantity: number
}

export interface StaffRequest {
  id: string
  roomNumber: string
  text: string
  status: RequestStatus
  createdAt: string
  updatedAt?: string
  requestItems: RequestItemView[]
}

interface RequestToast {
  id: string
  roomNumber: string
  text: string
}

interface NavigationItem {
  label: string
  to: string
  icon: string
}

interface TabItem {
  key: RequestTab
  label: string
  count: number
}

interface StatCardItem {
  key: string
  label: string
  value: number
  icon: string
  iconColorClass: string
}

interface DashboardMessage {
  type: MessageType
  text: string
}

interface BackendRequestDto {
  id: string
  roomNumber: string
  status: string
  createdAt: string
  updatedAt?: string
  text?: string
  requestItems?: RequestItemView[]
}

interface RequestSocketEvent {
  type: 'REQUEST_CREATED' | 'REQUEST_UPDATED'
  payload: BackendRequestDto
}

const route = useRoute()

const selectedTab = ref<RequestTab>('active')
const theme = ref<ThemeMode>('dark')
const requests = ref<StaffRequest[]>([])
const lowStockItems = ref<number>(0)
const activeToast = ref<RequestToast | null>(null)
const dashboardMessage = ref<DashboardMessage | null>(null)
const notificationAudio = ref<HTMLAudioElement | null>(null)

let toastTimeoutId: number | null = null
let socket: WebSocket | null = null

const navigationItems: NavigationItem[] = [
  { label: 'Dashboard', to: '/staff', icon: 'bi bi-grid' },
  { label: 'Inventory', to: '/staff/inventory', icon: 'bi bi-box-seam' },
  { label: 'Reports', to: '/staff/reports', icon: 'bi bi-bar-chart' },
  { label: 'Stocktaking', to: '/staff/stocktaking', icon: 'bi bi-clipboard-check' },
]

const activeRequests = computed<number>(() =>
    requests.value.filter(
        (request) => request.status === 'received' || request.status === 'in_progress'
    ).length
)

const totalRequests = computed<number>(() => requests.value.length)

const completedToday = computed<number>(() =>
    requests.value.filter((request) => request.status === 'delivered').length
)

const visibleRequests = computed<StaffRequest[]>(() => {
  switch (selectedTab.value) {
    case 'active':
      return requests.value.filter(
          (request) => request.status === 'received' || request.status === 'in_progress'
      )
    case 'all':
    default:
      return requests.value
  }
})

const tabs = computed<TabItem[]>(() => [
  { key: 'active', label: 'Active', count: activeRequests.value },
  { key: 'all', label: 'All', count: totalRequests.value },
])

const statsCards = computed<StatCardItem[]>(() => [
  {
    key: 'active-requests',
    label: 'Active Requests',
    value: activeRequests.value,
    icon: 'bi bi-clock-history',
    iconColorClass: 'icon-blue',
  },
  {
    key: 'total-requests',
    label: 'Total Requests',
    value: totalRequests.value,
    icon: 'bi bi-box',
    iconColorClass: 'icon-purple',
  },
  {
    key: 'completed-today',
    label: 'Completed Today',
    value: completedToday.value,
    icon: 'bi bi-check-circle',
    iconColorClass: 'icon-green',
  },
  {
    key: 'low-stock-items',
    label: 'Low Stock Items',
    value: lowStockItems.value,
    icon: 'bi bi-exclamation-triangle',
    iconColorClass: 'icon-orange',
  },
])

function normalizeStatus(status: string | undefined): RequestStatus {
  switch (status) {
    case 'IN_PROGRESS':
    case 'in_progress':
      return 'in_progress'
    case 'DELIVERED':
    case 'delivered':
      return 'delivered'
    case 'REJECTED':
    case 'rejected':
      return 'rejected'
    case 'RECEIVED':
    case 'received':
    default:
      return 'received'
  }
}

function buildFallbackText(items: RequestItemView[]): string {
  if (items.length === 0) {
    return 'Request received'
  }

  return items.map((item) => `${item.quantity} × ${item.itemName}`).join(', ')
}

function normalizeRequest(dto: BackendRequestDto): StaffRequest {
  return {
    id: dto.id,
    roomNumber: dto.roomNumber,
    text: dto.text?.trim() || buildFallbackText(dto.requestItems ?? []),
    status: normalizeStatus(dto.status),
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
    requestItems: dto.requestItems ?? [],
  }
}

function upsertRequest(nextRequest: StaffRequest): void {
  const existingIndex = requests.value.findIndex((request) => request.id === nextRequest.id)

  if (existingIndex === -1) {
    requests.value.unshift(nextRequest)
    return
  }

  requests.value[existingIndex] = nextRequest
}

function showToast(request: StaffRequest): void {
  if (toastTimeoutId !== null) {
    window.clearTimeout(toastTimeoutId)
  }

  activeToast.value = {
    id: request.id,
    roomNumber: request.roomNumber,
    text: request.text,
  }

  toastTimeoutId = window.setTimeout(() => {
    activeToast.value = null
    toastTimeoutId = null
  }, 4500)
}

async function playNotificationSound(): Promise<void> {
  try {
    await notificationAudio.value?.play()
  } catch {
  }
}

async function handleIncomingCreatedRequest(dto: BackendRequestDto): Promise<void> {
  const normalized = normalizeRequest(dto)
  upsertRequest(normalized)
  showToast(normalized)
  await playNotificationSound()
}

function handleIncomingUpdatedRequest(dto: BackendRequestDto): void {
  const normalized = normalizeRequest(dto)
  upsertRequest(normalized)
}

async function loadRequests(): Promise<void> {
  dashboardMessage.value = null

  await getServiceRequests()
      .then((data) => {
        requests.value = data.map(normalizeRequest)
      })
      .catch(() => {
        dashboardMessage.value = {
          type: 'error',
          text: 'Failed to load service requests. Please refresh and try again.',
        }
      })
}

function connectRealtimeChannel(): void {
  /**
   * Backend placeholder:
   * Replace with final WS endpoint.
   *
   * Example:
   * socket = new WebSocket('ws://localhost:8080/ws/staff/requests')
   * socket.onmessage = async (event) => {
   *   const message: RequestSocketEvent = JSON.parse(event.data)
   *   if (message.type === 'REQUEST_CREATED') {
   *     await handleIncomingCreatedRequest(message.payload)
   *     return
   *   }
   *   if (message.type === 'REQUEST_UPDATED') {
   *     handleIncomingUpdatedRequest(message.payload)
   *   }
   * }
   */
  void ({} as RequestSocketEvent)
}

function disconnectRealtimeChannel(): void {
  socket?.close()
  socket = null
}

function setStatus(id: string, status: RequestStatus): void {
  const request = requests.value.find((item) => item.id === id)

  if (!request) {
    return
  }

  const previousStatus = request.status
  request.status = status

  void previousStatus
}

function toggleTheme(): void {
  theme.value = theme.value === 'dark' ? 'light' : 'dark'
  localStorage.setItem('staff-theme', theme.value)
}

onMounted(async () => {
  const savedTheme = localStorage.getItem('staff-theme')

  if (savedTheme === 'light' || savedTheme === 'dark') {
    theme.value = savedTheme
  }

  await loadRequests()
  connectRealtimeChannel()
})

onBeforeUnmount(() => {
  disconnectRealtimeChannel()

  if (toastTimeoutId !== null) {
    window.clearTimeout(toastTimeoutId)
  }
})

watch(
    theme,
    (value) => {
      document.documentElement.setAttribute('data-theme', value)
    },
    { immediate: true }
)
</script>

<style scoped>
.staff-dashboard {
  background-color: var(--page-bg);
  color: var(--text-main);
  transition: background-color 0.2s ease, color 0.2s ease;
}

.main-content {
  min-width: 0;
}

.main-content__inner {
  min-height: 100vh;
}

.dashboard-toolbar {
  position: sticky;
  top: 0;
  z-index: 5;
  padding-top: 0.25rem;
  background: linear-gradient(180deg, var(--page-bg) 0%, var(--page-bg) 88%, transparent 100%);
}

.icon-blue {
  color: var(--primary);
}

.icon-purple {
  color: #a855f7;
}

.icon-green {
  color: var(--success);
}

.icon-orange {
  color: #f97316;
}

.staff-toast {
  position: fixed;
  top: 1.5rem;
  right: 1.5rem;
  width: min(360px, calc(100vw - 2rem));
  z-index: 1050;
  background: var(--card-bg);
  border: 1px solid var(--border-color);
  color: var(--text-main);
}

.staff-toast__text {
  color: var(--text-muted);
}

.toast-slide-enter-active,
.toast-slide-leave-active {
  transition: all 0.22s ease;
}

.toast-slide-enter-from,
.toast-slide-leave-to {
  opacity: 0;
  transform: translateY(-8px);
}

@media (max-width: 991.98px) {
  .main-content__inner {
    min-height: auto;
  }

  .dashboard-toolbar {
    position: static;
    background: transparent;
    padding-top: 0;
  }
}
</style>
