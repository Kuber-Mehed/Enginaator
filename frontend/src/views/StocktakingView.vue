<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import Sidebar from '../components/common/Sidebar.vue'
import ThemeToggleButton from '../components/common/ThemeToggleButton.vue'
import { useTheme } from '../utils/composables/useTheme'
import type {
  DiscrepancyReason,
  StocktakingSessionItem,
  StocktakingWorkingItem,
  ReconciliationItemView,
} from '../utils/types/stocktaking'

interface NavigationItem {
  label: string
  to: string
  icon: string
}

const route = useRoute()
const { theme, toggleTheme } = useTheme()

const navigationItems: NavigationItem[] = [
  { label: 'Dashboard', to: '/staff', icon: 'bi bi-grid' },
  { label: 'Inventory', to: '/staff/inventory', icon: 'bi bi-box-seam' },
  { label: 'Reports', to: '/staff/reports', icon: 'bi bi-bar-chart' },
  { label: 'Stocktaking', to: '/staff/stocktaking', icon: 'bi bi-clipboard-check' },
]

type Phase = 'idle' | 'counting'
const phase = ref<Phase>('idle')

const sessionItems = ref<StocktakingWorkingItem[]>([])
const recentItems = ref<ReconciliationItemView[]>([])
const isLoading = ref(false)
const apiError = ref<string | null>(null)

const discrepancyCount = computed(() =>
  sessionItems.value.filter(i => i.physicalCount !== i.expectedCount).length
)

const isSubmittable = computed(() => {
  if (discrepancyCount.value === 0) return true
  return sessionItems.value
    .filter(i => i.physicalCount !== i.expectedCount)
    .every(i => i.reason !== null)
})

async function openSession(): Promise<void> {
  isLoading.value = true
  apiError.value = null
  try {
    const res = await fetch('/api/staff/stocktaking/session')
    if (!res.ok) throw new Error('Failed to load inventory')
    const data: StocktakingSessionItem[] = await res.json()
    sessionItems.value = data.map(item => ({
      ...item,
      physicalCount: item.expectedCount,
      reason: null,
    }))
    phase.value = 'counting'
  } catch (e) {
    apiError.value = e instanceof Error ? e.message : 'Failed to start session'
  } finally {
    isLoading.value = false
  }
}

async function submitReconciliation(): Promise<void> {
  if (!isSubmittable.value) return
  isLoading.value = true
  apiError.value = null
  try {
    const body = {
      items: sessionItems.value.map(i => ({
        itemId: i.id,
        physicalCount: i.physicalCount,
        reason: i.physicalCount !== i.expectedCount ? i.reason : null,
      })),
    }
    const res = await fetch('/api/staff/stocktaking/submit', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    })
    if (!res.ok) throw new Error('Failed to submit reconciliation')
    const result = await res.json()
    recentItems.value = (result.items as ReconciliationItemView[]).filter(
      i => i.discrepancy !== 0
    )
    sessionItems.value = []
    phase.value = 'idle'
  } catch (e) {
    apiError.value = e instanceof Error ? e.message : 'Submission failed'
  } finally {
    isLoading.value = false
  }
}

function cancelSession(): void {
  phase.value = 'idle'
  sessionItems.value = []
  apiError.value = null
}

function updatePhysicalCount(itemId: string, value: number): void {
  const item = sessionItems.value.find(i => i.id === itemId)
  if (!item) return
  item.physicalCount = Math.max(0, isNaN(value) ? 0 : value)
  if (item.physicalCount === item.expectedCount) {
    item.reason = null
  }
}

function updateReason(itemId: string, value: string): void {
  const item = sessionItems.value.find(i => i.id === itemId)
  if (item) item.reason = value ? (value as DiscrepancyReason) : null
}

function discrepancyOf(item: StocktakingWorkingItem): number {
  return item.physicalCount - item.expectedCount
}

function discrepancyColorClass(n: number): string {
  if (n < 0) return 'text-danger'
  if (n > 0) return 'text-success'
  return 'text-secondary'
}

function formatDiscrepancy(n: number): string {
  return n > 0 ? `+${n}` : String(n)
}

function formatReason(reason: DiscrepancyReason): string {
  return reason.toLowerCase()
}

onMounted(async () => {
  try {
    const res = await fetch('/api/staff/stocktaking/history')
    if (res.ok) {
      const data: { items: ReconciliationItemView[] }[] = await res.json()
      if (data.length > 0) {
        recentItems.value = data[0].items.filter(i => i.discrepancy !== 0)
      }
    }
  } catch {
    // Leave history empty until backend is available
  }
})
</script>

<template>
  <div class="stocktaking-view min-vh-100">
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
              <h1 class="fw-bold fs-2 mb-1">Stocktaking Reconciliation</h1>
              <p class="text-secondary mb-0">Perform physical inventory counts and reconcile discrepancies</p>
            </div>

            <!-- Error alert -->
            <div v-if="apiError" class="alert alert-danger rounded-4 mb-4">{{ apiError }}</div>

            <!-- ── COUNTING PHASE ─────────────────────────────────── -->
            <template v-if="phase === 'counting'">

              <!-- Session banner -->
              <div class="session-banner rounded-4 p-4 mb-4 d-flex align-items-center justify-content-between flex-wrap gap-3">
                <div class="d-flex align-items-center gap-3">
                  <i class="bi bi-info-circle session-icon fs-5"></i>
                  <div>
                    <div class="fw-semibold session-title">Stocktaking Session Active</div>
                    <div class="small session-subtitle">{{ discrepancyCount }} item(s) with discrepancies</div>
                  </div>
                </div>
                <div class="d-flex gap-2">
                  <button @click="cancelSession" class="btn btn-cancel rounded-4 px-4 fw-semibold">
                    Cancel
                  </button>
                  <button
                    @click="submitReconciliation"
                    :disabled="!isSubmittable || isLoading"
                    class="btn btn-complete rounded-4 px-4 fw-semibold"
                  >
                    <i v-if="!isSubmittable" class="bi bi-clock me-1 opacity-75"></i>
                    <i v-else-if="isLoading" class="bi bi-arrow-repeat me-1"></i>
                    Complete Stocktaking
                  </button>
                </div>
              </div>

              <!-- Counting table -->
              <div class="table-card rounded-4 overflow-hidden">
                <table class="table table-borderless mb-0">
                  <thead>
                    <tr class="table-header-row">
                      <th class="col-header ps-4 py-3">ITEM</th>
                      <th class="col-header text-center py-3">EXPECTED COUNT</th>
                      <th class="col-header text-center py-3">PHYSICAL COUNT</th>
                      <th class="col-header text-center py-3">DISCREPANCY</th>
                      <th class="col-header py-3">REASON (IF DISCREPANCY)</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in sessionItems" :key="item.id" class="item-row">
                      <td class="ps-4 py-3 align-middle">
                        <div class="d-flex align-items-center gap-3">
                          <div class="item-icon rounded-3 d-flex align-items-center justify-content-center flex-shrink-0">
                            <i class="bi bi-box-seam"></i>
                          </div>
                          <div>
                            <div class="fw-semibold">{{ item.name }}</div>
                            <div class="small text-secondary">{{ item.category }}</div>
                          </div>
                        </div>
                      </td>
                      <td class="text-center align-middle fw-semibold py-3">{{ item.expectedCount }}</td>
                      <td class="text-center align-middle py-3">
                        <input
                          type="number"
                          :value="item.physicalCount"
                          @input="updatePhysicalCount(item.id, +($event.target as HTMLInputElement).value)"
                          class="count-input text-center rounded-3"
                          min="0"
                        />
                      </td>
                      <td
                        class="text-center align-middle fw-semibold py-3"
                        :class="discrepancyColorClass(discrepancyOf(item))"
                      >
                        {{ discrepancyOf(item) === 0 ? '0' : formatDiscrepancy(discrepancyOf(item)) }}
                      </td>
                      <td class="align-middle py-3 pe-4">
                        <select
                          v-if="discrepancyOf(item) !== 0"
                          :value="item.reason ?? ''"
                          @change="updateReason(item.id, ($event.target as HTMLSelectElement).value)"
                          class="reason-select rounded-3"
                          :class="{ 'reason-missing': item.reason === null }"
                        >
                          <option value="">Select reason...</option>
                          <option value="DAMAGED">Damaged</option>
                          <option value="THEFT">Theft</option>
                          <option value="MISCOUNTED">Miscounted</option>
                          <option value="SUPPLIER_ERROR">Supplier Error</option>
                        </select>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>

            </template>

            <!-- ── IDLE PHASE ──────────────────────────────────────── -->
            <template v-else>

              <!-- Start card -->
              <div class="table-card rounded-4 p-5 mb-4 text-center">
                <div class="mb-4">
                  <i class="bi bi-clipboard-check-fill start-icon"></i>
                </div>
                <h3 class="fw-bold mb-3">Ready to Start Stocktaking?</h3>
                <p class="text-secondary mb-4 mx-auto" style="max-width: 520px;">
                  This will lock in the current expected counts as a baseline. Enter the physical count for each
                  item, and the system will calculate discrepancies.
                </p>
                <button
                  @click="openSession"
                  :disabled="isLoading"
                  class="btn btn-primary btn-start rounded-4 px-5 py-3 fw-semibold"
                >
                  {{ isLoading ? 'Loading...' : 'Start Stocktaking Session' }}
                </button>
              </div>

              <!-- Recent Reconciliations -->
              <div class="table-card rounded-4 p-4">
                <h5 class="fw-bold mb-4">Recent Reconciliations</h5>
                <table v-if="recentItems.length > 0" class="table table-borderless mb-0">
                  <thead>
                    <tr class="table-header-row">
                      <th class="col-header py-3">ITEM</th>
                      <th class="col-header text-center py-3">EXPECTED</th>
                      <th class="col-header text-center py-3">PHYSICAL</th>
                      <th class="col-header text-center py-3">DISCREPANCY</th>
                      <th class="col-header py-3">REASON</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr v-for="item in recentItems" :key="item.itemId" class="item-row">
                      <td class="fw-semibold align-middle py-3">{{ item.itemName }}</td>
                      <td class="text-center align-middle py-3">{{ item.expectedCount }}</td>
                      <td class="text-center align-middle py-3">{{ item.physicalCount }}</td>
                      <td
                        class="text-center align-middle fw-semibold py-3"
                        :class="discrepancyColorClass(item.discrepancy)"
                      >
                        {{ formatDiscrepancy(item.discrepancy) }}
                      </td>
                      <td class="align-middle py-3">
                        <span v-if="item.reason" class="reason-badge">{{ formatReason(item.reason) }}</span>
                      </td>
                    </tr>
                  </tbody>
                </table>
                <div v-else class="text-secondary text-center py-4">
                  No previous reconciliations.
                </div>
              </div>

            </template>

          </div>
        </main>
      </div>
    </div>
  </div>
</template>

<style scoped>
.stocktaking-view {
  background-color: var(--page-bg);
  color: var(--text-main);
  transition: background-color 0.2s ease, color 0.2s ease;
}

.main-content {
  min-width: 0;
}

/* ── Session banner ───────────────────────────── */
.session-banner {
  background: rgba(37, 99, 235, 0.06);
  border: 1.5px solid rgba(96, 165, 250, 0.35);
}

.session-icon,
.session-title {
  color: var(--primary);
}

.session-subtitle {
  color: var(--primary);
  opacity: 0.8;
}

/* ── Buttons ──────────────────────────────────── */
.btn-cancel {
  background: rgba(100, 116, 139, 0.12);
  color: var(--text-main);
  border: 1px solid rgba(100, 116, 139, 0.3);
}

.btn-cancel:hover {
  background: rgba(100, 116, 139, 0.22);
  color: var(--text-main);
  border-color: rgba(100, 116, 139, 0.4);
}

.btn-complete {
  background: var(--primary);
  color: #fff;
  border: 1px solid var(--primary);
}

.btn-complete:hover:not(:disabled) {
  filter: brightness(1.1);
  color: #fff;
}

.btn-complete:disabled {
  background: rgba(100, 116, 139, 0.2) !important;
  border-color: rgba(100, 116, 139, 0.2) !important;
  color: var(--text-muted) !important;
  opacity: 1;
}

/* ── Table card ───────────────────────────────── */
.table-card {
  background-color: var(--card-bg);
}

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

/* ── Item icon ────────────────────────────────── */
.item-icon {
  width: 38px;
  height: 38px;
  background: rgba(37, 99, 235, 0.14);
  color: var(--primary);
  font-size: 1rem;
}

/* ── Physical count input ─────────────────────── */
.count-input {
  width: 90px;
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-color);
  color: var(--text-main);
  padding: 0.4rem 0.5rem;
  font-size: 1rem;
  font-weight: 600;
  outline: none;
  transition: border-color 0.15s ease;
}

.count-input:focus {
  border-color: var(--primary);
}

/* ── Reason select ────────────────────────────── */
.reason-select {
  background: rgba(255, 255, 255, 0.05);
  border: 1px solid var(--border-color);
  color: var(--text-main);
  padding: 0.4rem 0.6rem;
  font-size: 0.875rem;
  outline: none;
  min-width: 150px;
  cursor: pointer;
  transition: border-color 0.15s ease;
}

.reason-select:focus {
  border-color: var(--primary);
}

.reason-select.reason-missing {
  border-color: var(--danger);
}

/* ── Reason badge (history) ───────────────────── */
.reason-badge {
  display: inline-block;
  background: rgba(148, 163, 184, 0.12);
  color: var(--text-muted);
  padding: 0.2rem 0.6rem;
  border-radius: 0.375rem;
  font-size: 0.8rem;
  font-weight: 500;
}

/* ── Start card icon ──────────────────────────── */
.start-icon {
  font-size: 4.5rem;
  color: var(--primary);
}

.btn-start {
  font-size: 1rem;
  min-width: 240px;
}
</style>
