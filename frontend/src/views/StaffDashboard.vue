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

            <RequestList
                :requests="visibleRequests"
                @open-request="openRequest"
            />
          </div>

          <!-- 🔔 Notification (queue-based, temporary UI) -->
          <Transition name="toast-slide">
            <div v-if="activeNotification" class="staff-toast shadow-lg rounded-4 p-3">
              <div class="small text-uppercase fw-semibold text-secondary-emphasis mb-1">
                New request
              </div>
              <div class="fw-bold mb-1">Room {{ activeNotification.roomNumber }}</div>
              <div class="staff-toast__text">{{ activeNotification.text }}</div>

              <div class="d-flex gap-2 mt-3">
                <button class="btn btn-sm btn-success" @click="acknowledgeNotification">
                  Accept
                </button>
                <button class="btn btn-sm btn-outline-secondary" @click="dismissNotification">
                  Close
                </button>
              </div>
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
import { eventBus } from '@/services/event-bus.ts'

type RequestStatus =
    | 'RECEIVED'
    | 'IN_PROGRESS'
    | 'DELIVERED'
    | 'REJECTED'
    | 'CANCELLED'

type RequestTab = 'active' | 'all'
type ThemeMode = 'dark' | 'light'
type MessageType = 'info' | 'error'

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

interface BackendRequestDto {
  id: string
  roomNumber: string
  status: string
  createdAt: string
  updatedAt?: string
  text?: string
  requestItems?: RequestItemView[]
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

const route = useRoute()

const selectedTab = ref<RequestTab>('active')
const theme = ref<ThemeMode>('dark')
const requests = ref<StaffRequest[]>([])
const dashboardMessage = ref<DashboardMessage | null>(null)
const notificationAudio = ref<HTMLAudioElement | null>(null)

/**
 * 🔔 NEW: notification queue (instead of single toast)
 */
const notificationQueue = ref<StaffRequest[]>([])

const activeNotification = computed(() => notificationQueue.value[0] ?? null)

let socket: WebSocket | null = null

const navigationItems: NavigationItem[] = [
  { label: 'Dashboard', to: '/staff', icon: 'bi bi-grid' },
  { label: 'Inventory', to: '/staff/inventory', icon: 'bi bi-box-seam' },
  { label: 'Reports', to: '/staff/reports', icon: 'bi bi-bar-chart' },
  { label: 'Stocktaking', to: '/staff/stocktaking', icon: 'bi bi-clipboard-check' },
]

const activeRequests = computed(() =>
    requests.value.filter(
        (r) => r.status === 'RECEIVED' || r.status === 'IN_PROGRESS'
    ).length
)

const totalRequests = computed(() => requests.value.length)

const completedToday = computed(() =>
    requests.value.filter((r) => r.status === 'DELIVERED').length
)

const visibleRequests = computed(() => {
  if (selectedTab.value === 'active') {
    return requests.value.filter(
        (r) => r.status === 'RECEIVED' || r.status === 'IN_PROGRESS'
    )
  }
  return requests.value
})

const tabs = computed<TabItem[]>(() => [
  { key: 'active', label: 'Active', count: activeRequests.value },
  { key: 'all', label: 'All', count: totalRequests.value },
])

const statsCards = computed<StatCardItem[]>(() => [
  {
    key: 'active',
    label: 'Active Requests',
    value: activeRequests.value,
    icon: 'bi bi-clock-history',
    iconColorClass: 'icon-blue',
  },
  {
    key: 'total',
    label: 'Total Requests',
    value: totalRequests.value,
    icon: 'bi bi-box',
    iconColorClass: 'icon-purple',
  },
  {
    key: 'completed',
    label: 'Completed Today',
    value: completedToday.value,
    icon: 'bi bi-check-circle',
    iconColorClass: 'icon-green',
  },
])

function normalizeStatus(status?: string): RequestStatus {
  return (status?.toUpperCase() as RequestStatus) || 'RECEIVED'
}

function buildFallbackText(items: RequestItemView[]): string {
  return items.length === 0
      ? 'Request received'
      : items.map(i => `${i.quantityRequested} × ${i.itemName}`).join(', ')
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

function upsertRequest(req: StaffRequest) {
  const i = requests.value.findIndex(r => r.id === req.id)
  if (i === -1) {
    requests.value.unshift(req)
  } else {
    requests.value[i] = req
  }
}

/**
 * 🔔 Notification queue logic
 */
function enqueueNotification(req: StaffRequest) {
  notificationQueue.value.push(req)
}

function dismissNotification() {
  notificationQueue.value.shift()
}

function acknowledgeNotification() {
  const req = notificationQueue.value.shift()
  if (!req) return

  // TEMP: just open request (later → modal + backend call)
  openRequest(req)
}

async function playSound() {
  try {
    await notificationAudio.value?.play()
  } catch {}
}

async function handleNewRequest(dto: BackendRequestDto) {
  const req = normalizeRequest(dto)
  upsertRequest(req)
  enqueueNotification(req)
  await playSound()
}

function handleUpdate(dto: BackendRequestDto) {
  upsertRequest(normalizeRequest(dto))
}

async function loadRequests() {
  try {
    const data = await getServiceRequests()
    requests.value = data.map(normalizeRequest)
  } catch {
    dashboardMessage.value = {
      type: 'error',
      text: 'Failed to load service requests',
    }
  }
}

function openRequest(req: StaffRequest) {
  console.log('OPEN REQUEST (future modal)', req)
}

function connectRealtimeChannel() {
  // TODO: replace with real WebSocket
}

function disconnectRealtimeChannel() {
  socket?.close()
}

function toggleTheme() {
  theme.value = theme.value === 'dark' ? 'light' : 'dark'
  localStorage.setItem('staff-theme', theme.value)
}

onMounted(async () => {
  const saved = localStorage.getItem('staff-theme')
  if (saved === 'light' || saved === 'dark') {
    theme.value = saved
  }

  await loadRequests()
  connectRealtimeChannel()

  eventBus.on('new-request', async (data) => {
    await handleNewRequest(data as BackendRequestDto)
  })
})

onBeforeUnmount(() => {
  disconnectRealtimeChannel()
})

watch(theme, (val) => {
  document.documentElement.setAttribute('data-theme', val)
}, { immediate: true })
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
