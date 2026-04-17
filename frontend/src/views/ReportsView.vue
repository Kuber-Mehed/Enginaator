<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from '../components/common/Sidebar.vue'
import ThemeToggleButton from '../components/common/ThemeToggleButton.vue'
import { useTheme } from '../utils/composables/useTheme'

interface NavigationItem {
  label: string
  to: string
  icon: string
}

interface ItemConsumptionRow {
  itemName: string
  quantityConsumed: number
  requestCount: number
  avgPerRequest: number
}

interface ReconciliationSummaryRow {
  itemName: string
  adjustment: number
  reason: string
  date: string
}

interface MonthlyReportData {
  month: string
  totalRequests: number
  totalItemsConsumed: number
  uniqueItemTypes: number
  reconciliationCount: number
  itemConsumption: ItemConsumptionRow[]
  reconciliationSummary: ReconciliationSummaryRow[]
}

const route = useRoute()
const { theme, toggleTheme } = useTheme()

const navigationItems: NavigationItem[] = [
  { label: 'Dashboard', to: '/staff', icon: 'bi bi-grid' },
  { label: 'Inventory', to: '/staff/inventory', icon: 'bi bi-box-seam' },
  { label: 'Reports', to: '/staff/reports', icon: 'bi bi-bar-chart' },
  { label: 'Stocktaking', to: '/staff/stocktaking', icon: 'bi bi-clipboard-check' },
]

function currentYearMonth(): string {
  const now = new Date()
  const y = now.getFullYear()
  const m = String(now.getMonth() + 1).padStart(2, '0')
  return `${y}-${m}`
}

const selectedMonth = ref<string>(currentYearMonth())

const totalRequests = ref(0)
const totalItemsConsumed = ref(0)
const uniqueItemTypes = ref(0)
const reconciliationCount = ref(0)
const itemConsumption = ref<ItemConsumptionRow[]>([])
const reconciliationSummary = ref<ReconciliationSummaryRow[]>([])
const isLoading = ref(false)
const apiError = ref<string | null>(null)

const formattedMonth = computed(() => {
  const [year, month] = selectedMonth.value.split('-')
  const date = new Date(Number(year), Number(month) - 1)
  return date.toLocaleString('default', { month: 'short', year: 'numeric' })
})

async function loadReport(): Promise<void> {
  isLoading.value = true
  apiError.value = null
  try {
    const res = await fetch(`/api/staff/reports/monthly?month=${selectedMonth.value}`)
    if (!res.ok) throw new Error('Failed to load report')
    const data: MonthlyReportData = await res.json()
    totalRequests.value = data.totalRequests
    totalItemsConsumed.value = data.totalItemsConsumed
    uniqueItemTypes.value = data.uniqueItemTypes
    reconciliationCount.value = data.reconciliationCount
    itemConsumption.value = data.itemConsumption
    reconciliationSummary.value = data.reconciliationSummary
  } catch (e) {
    apiError.value = e instanceof Error ? e.message : 'Failed to load report'
    totalRequests.value = 0
    totalItemsConsumed.value = 0
    uniqueItemTypes.value = 0
    reconciliationCount.value = 0
    itemConsumption.value = []
    reconciliationSummary.value = []
  } finally {
    isLoading.value = false
  }
}

function formatAdjustment(n: number): string {
  return n > 0 ? `+${n}` : String(n)
}

function adjustmentColorClass(n: number): string {
  if (n < 0) return 'text-danger'
  if (n > 0) return 'text-success'
  return ''
}

function formatDate(dateStr: string): string {
  const date = new Date(dateStr)
  return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })
}

onMounted(() => {
  loadReport()
})
</script>

<template>
  <div class="reports-view min-vh-100">
    <div class="container-fluid px-0">
      <div class="row g-0 min-vh-100">
        <Sidebar :items="navigationItems" :current-path="route.path">
          <template #bottom>
            <ThemeToggleButton :theme="theme" @toggle="toggleTheme" />
          </template>
        </Sidebar>

        <main class="col main-content">
          <div class="p-4 p-xl-5">

            <!-- Page Header -->
            <div class="mb-5">
              <h1 class="fw-bold fs-2 mb-1">Monthly Reports</h1>
              <p class="text-secondary mb-0">View consumption and usage statistics</p>
            </div>

            <!-- Month Selector -->
            <div class="table-card rounded-4 p-4 mb-4 d-flex align-items-center justify-content-between">
              <div class="d-flex align-items-center gap-3">
                <i class="bi bi-calendar3 text-secondary fs-5"></i>
                <span class="fw-semibold">Select Month:</span>
              </div>
              <input
                v-model="selectedMonth"
                @change="loadReport"
                type="month"
                class="month-input rounded-3"
              />
            </div>

            <!-- Error alert -->
            <div v-if="apiError" class="alert alert-danger rounded-4 mb-4">{{ apiError }}</div>

            <!-- Stat Cards -->
            <div class="row g-3 mb-4">
              <div class="col-12 col-md-4">
                <div class="table-card rounded-4 p-4">
                  <div class="d-flex justify-content-between align-items-start mb-3">
                    <span class="stat-label">Total Requests</span>
                    <i class="bi bi-send-check stat-icon stat-icon--blue fs-5"></i>
                  </div>
                  <div class="stat-value">{{ totalRequests }}</div>
                  <div class="small text-secondary mt-1">{{ formattedMonth }}</div>
                </div>
              </div>
              <div class="col-12 col-md-4">
                <div class="table-card rounded-4 p-4">
                  <div class="d-flex justify-content-between align-items-start mb-3">
                    <span class="stat-label">Items Consumed</span>
                    <i class="bi bi-graph-up-arrow stat-icon stat-icon--teal fs-5"></i>
                  </div>
                  <div class="stat-value">{{ totalItemsConsumed }}</div>
                  <div class="small text-secondary mt-1">Across {{ uniqueItemTypes }} item types</div>
                </div>
              </div>
              <div class="col-12 col-md-4">
                <div class="table-card rounded-4 p-4">
                  <div class="d-flex justify-content-between align-items-start mb-3">
                    <span class="stat-label">Reconciliations</span>
                    <i class="bi bi-bar-chart-line stat-icon stat-icon--indigo fs-5"></i>
                  </div>
                  <div class="stat-value">{{ reconciliationCount }}</div>
                  <div class="small text-secondary mt-1">Stocktaking events</div>
                </div>
              </div>
            </div>

            <!-- Item Consumption -->
            <div class="table-card rounded-4 overflow-hidden mb-4">
              <div class="p-4 pb-0">
                <h5 class="fw-bold mb-0">Item Consumption</h5>
              </div>
              <div class="table-responsive mt-3">
                <table class="table table-borderless mb-0">
                  <thead>
                    <tr class="table-header-row">
                      <th class="col-header ps-4 py-3">ITEM</th>
                      <th class="col-header py-3">QUANTITY CONSUMED</th>
                      <th class="col-header py-3"># OF REQUESTS</th>
                      <th class="col-header pe-4 py-3">AVG PER REQUEST</th>
                    </tr>
                  </thead>
                  <tbody>
                    <template v-if="itemConsumption.length > 0">
                      <tr v-for="row in itemConsumption" :key="row.itemName" class="item-row">
                        <td class="ps-4 py-3 fw-semibold align-middle">{{ row.itemName }}</td>
                        <td class="py-3 align-middle">{{ row.quantityConsumed }}</td>
                        <td class="py-3 align-middle">{{ row.requestCount }}</td>
                        <td class="pe-4 py-3 align-middle">{{ row.avgPerRequest.toFixed(1) }}</td>
                      </tr>
                    </template>
                    <tr v-else>
                      <td colspan="4" class="py-5 text-center">
                        <div class="d-flex flex-column align-items-center gap-3">
                          <i class="bi bi-bar-chart empty-icon"></i>
                          <div>
                            <div class="fw-semibold mb-1">No data for this month</div>
                            <div class="text-secondary small">No completed requests found in {{ formattedMonth }}</div>
                          </div>
                        </div>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

            <!-- Reconciliation Summary -->
            <div class="table-card rounded-4 overflow-hidden">
              <div class="p-4 pb-0">
                <h5 class="fw-bold mb-0">Reconciliation Summary</h5>
              </div>
              <div class="table-responsive mt-3">
                <table class="table table-borderless mb-0">
                  <thead>
                    <tr class="table-header-row">
                      <th class="col-header ps-4 py-3">ITEM</th>
                      <th class="col-header py-3">ADJUSTMENT</th>
                      <th class="col-header py-3">REASON</th>
                      <th class="col-header pe-4 py-3 text-end">DATE</th>
                    </tr>
                  </thead>
                  <tbody>
                    <template v-if="reconciliationSummary.length > 0">
                      <tr
                        v-for="row in reconciliationSummary"
                        :key="`${row.itemName}-${row.date}`"
                        class="item-row"
                      >
                        <td class="ps-4 py-3 fw-semibold align-middle">{{ row.itemName }}</td>
                        <td
                          class="py-3 fw-semibold align-middle"
                          :class="adjustmentColorClass(row.adjustment)"
                        >
                          {{ formatAdjustment(row.adjustment) }}
                        </td>
                        <td class="py-3 align-middle text-secondary">{{ row.reason }}</td>
                        <td class="pe-4 py-3 align-middle text-end text-secondary">
                          {{ formatDate(row.date) }}
                        </td>
                      </tr>
                    </template>
                    <tr v-else>
                      <td colspan="4" class="py-4 text-center text-secondary">
                        No reconciliation data for this month.
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </div>

          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<style scoped>
.reports-view {
  background-color: var(--page-bg);
  color: var(--text-main);
  transition: background-color 0.2s ease, color 0.2s ease;
}

.main-content {
  min-width: 0;
}

/* ── Card ─────────────────────────────────────── */
.table-card {
  background-color: var(--card-bg);
}

/* ── Month input ──────────────────────────────── */
.month-input {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-color);
  color: var(--text-main);
  padding: 0.5rem 0.75rem;
  font-size: 0.95rem;
  outline: none;
  min-width: 160px;
  transition: border-color 0.15s ease;
  color-scheme: dark;
}

.month-input:focus {
  border-color: var(--primary);
}

/* ── Stat cards ───────────────────────────────── */
.stat-label {
  font-size: 0.875rem;
  color: var(--text-muted);
  font-weight: 500;
}

.stat-value {
  font-size: 2rem;
  font-weight: 700;
  line-height: 1;
}

.stat-icon--blue   { color: #3b82f6; }
.stat-icon--teal   { color: #14b8a6; }
.stat-icon--indigo { color: #818cf8; }

/* ── Table ────────────────────────────────────── */
.table-header-row {
  border-bottom: 1px solid var(--border-color);
}

.col-header {
  font-size: 0.72rem;
  font-weight: 600;
  color: var(--text-muted);
  letter-spacing: 0.06em;
  background: transparent;
  border: none;
}

.item-row {
  border-bottom: 1px solid var(--border-color);
}

.item-row:last-child {
  border-bottom: none;
}

/* ── Empty state ──────────────────────────────── */
.empty-icon {
  font-size: 3rem;
  color: var(--empty-icon);
}
</style>
