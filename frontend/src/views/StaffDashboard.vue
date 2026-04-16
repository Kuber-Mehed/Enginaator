<template>
  <div class="staff-dashboard min-vh-100">
    <div class="container-fluid px-0">
      <div class="row g-0 min-vh-100">
        <Sidebar :items="navigationItems" :current-path="route.path">
          <template #bottom>
            <ThemeToggleButton :theme="theme" @toggle="toggleTheme" />
          </template>
        </Sidebar>

        <main class="col main-content">
          <div class="p-4 p-xl-5">
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

            <section class="mb-4">
              <RequestFilters v-model="selectedTab" :tabs="tabs" />
            </section>

            <RequestList :requests="visibleRequests" @set-status="setStatus" />
          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
/**
 * Staff dashboard page.
 *
 * Responsibilities:
 * - compose reusable dashboard components
 * - manage local demo state for requests and theme
 * - later this can be connected to Pinia / API / WebSocket
 */

import { computed, onMounted, ref, watch } from 'vue'
import { useRoute } from 'vue-router'

import Sidebar from '../components/common/Sidebar.vue'
import ThemeToggleButton from '../components/common/ThemeToggleButton.vue'
import StatCard from '../components/common/StatCard.vue'
import RequestFilters from '../components/request/RequestFilters.vue'
import RequestList from '../components/request/RequestList.vue'

type RequestStatus = 'received' | 'in_progress' | 'delivered' | 'rejected'
type RequestTab = 'active' | 'all'
type ThemeMode = 'dark' | 'light'

interface RequestItem {
  id: string
  room: string
  text: string
  status: RequestStatus
  timeAgo: string
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

const route = useRoute()

/**
 * Sidebar navigation items.
 */
const navigationItems: NavigationItem[] = [
  { label: 'Dashboard', to: '/staff', icon: 'bi bi-grid' },
  { label: 'Inventory', to: '/staff/inventory', icon: 'bi bi-box-seam' },
  { label: 'Reports', to: '/staff/reports', icon: 'bi bi-bar-chart' },
  { label: 'Stocktaking', to: '/staff/stocktaking', icon: 'bi bi-clipboard-check' },
]

/**
 * Current filter tab.
 */
const selectedTab = ref<RequestTab>('active')

/**
 * Global theme mode.
 * Stored in localStorage and applied to <html data-theme="...">.
 */
const theme = ref<ThemeMode>('dark')

/**
 * Local request state for now.
 * Replace with API/store data later.
 */
const requests = ref<RequestItem[]>([])

/**
 * Placeholder for low stock count.
 * Replace with real inventory state later.
 */
const lowStockItems = ref<number>(0)

/**
 * Active requests = received + in progress.
 */
const activeRequests = computed<number>(() =>
    requests.value.filter(
        (request) =>
            request.status === 'received' || request.status === 'in_progress'
    ).length
)

/**
 * Total number of requests.
 */
const totalRequests = computed<number>(() => requests.value.length)

/**
 * Delivered requests count.
 * Later you can filter by real date.
 */
const completedToday = computed<number>(() =>
    requests.value.filter((request) => request.status === 'delivered').length
)

/**
 * Requests shown based on the selected tab.
 */
const visibleRequests = computed<RequestItem[]>(() => {
  switch (selectedTab.value) {
    case 'active':
      return requests.value.filter(
          (request) =>
              request.status === 'received' || request.status === 'in_progress'
      )
    case 'all':
    default:
      return requests.value
  }
})

/**
 * Filter tab config.
 */
const tabs = computed<TabItem[]>(() => [
  {
    key: 'active',
    label: 'Active',
    count: activeRequests.value,
  },
  {
    key: 'all',
    label: 'All',
    count: totalRequests.value,
  },
])

/**
 * Dashboard stats cards config.
 */
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

/**
 * Updates a request status locally.
 * Later this should call backend / store action.
 */
function setStatus(id: string, status: RequestStatus): void {
  const request = requests.value.find((item) => item.id === id)

  if (!request) {
    return
  }

  request.status = status
}

/**
 * Toggle between dark and light themes.
 */
function toggleTheme(): void {
  theme.value = theme.value === 'dark' ? 'light' : 'dark'
  localStorage.setItem('staff-theme', theme.value)
}

/**
 * Restore saved theme on mount.
 */
onMounted(() => {
  const savedTheme = localStorage.getItem('staff-theme')

  if (savedTheme === 'light' || savedTheme === 'dark') {
    theme.value = savedTheme
  }
})

/**
 * Keep the global HTML data-theme attribute in sync.
 * Global CSS variables come from theme.css.
 */
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
</style>