<template>
  <div class="inventory-view min-vh-100">
    <div class="container-fluid px-0">
      <div class="row g-0 min-vh-100">
        <Sidebar :items="navigationItems" :current-path="route.path">
          <template #bottom>
            <ThemeToggleButton :theme="theme" @toggle="toggleTheme" />
          </template>
        </Sidebar>

        <main class="col main-content">
          <div class="p-4 p-xl-5">
            <InventoryHeader @add-item="openCreateModal" />

            <section class="row g-4 mb-4">
              <div
                  v-for="card in statsCards"
                  :key="card.key"
                  class="col-12 col-md-6 col-xl-4"
              >
                <StatCard
                    :label="card.label"
                    :value="card.value"
                    :icon="card.icon"
                    :icon-color-class="card.iconColorClass"
                />
              </div>
            </section>

            <InventoryToolbar
                :search-query="searchQuery"
                :selected-category="selectedCategory"
                :categories="categories"
                @update:searchQuery="searchQuery = $event"
                @update:selectedCategory="selectedCategory = $event"
            />

            <InventoryTable
                :items="filteredItems"
                @edit="openEditModal"
                @delete="deleteInventoryItem"
            />
          </div>
        </main>
      </div>
    </div>

    <InventoryItemModal
        ref="inventoryModalRef"
        :form="form"
        :is-edit-mode="isEditMode"
        :available-units="availableUnits"
        :form-category-options="formCategoryOptions"
        @update:form="form = $event"
        @save="saveItem"
    />
  </div>
</template>

<script setup lang="ts">
/**
 * InventoryView
 *
 * Responsibilities:
 * - page composition
 * - inventory state
 * - computed filters and stats
 * - create/edit/delete actions
 *
 * UI-heavy parts are delegated to child components.
 */

import {computed, onMounted, ref, watch} from 'vue'
import { useRoute } from 'vue-router'

import Sidebar from '../components/common/Sidebar.vue'
import ThemeToggleButton from '../components/common/ThemeToggleButton.vue'
import StatCard from '../components/common/StatCard.vue'

import InventoryHeader from '../components/inventory/InventoryHeader.vue'
import InventoryToolbar from '../components/inventory/InventoryToolbar.vue'
import InventoryTable from '../components/inventory/InventoryTable.vue'
import InventoryItemModal from '../components/inventory/InventoryItemModal.vue'

import { useTheme } from '../utils/composables/useTheme'
import {
  EMPTY_INVENTORY_FORM,
  FORM_CATEGORY_OPTIONS,
  INVENTORY_CATEGORIES,
} from '../utils/constants/inventory'

import type { InventoryFormState, InventoryItem } from '../utils/types/inventory'
import {addItem, deleteItem, getInventory, updateItem} from "@/services/inventory-service.ts";

type CategoryFilter = typeof INVENTORY_CATEGORIES[number]

interface NavigationItem {
  label: string
  to: string
  icon: string
}

interface StatCardItem {
  key: string
  label: string
  value: number
  icon: string
  iconColorClass: string
}

interface InventoryModalExposed {
  show: () => void
  hide: () => void
}

const route = useRoute()
const { theme, toggleTheme } = useTheme()

const navigationItems: NavigationItem[] = [
  { label: 'Dashboard', to: '/staff', icon: 'bi bi-grid' },
  { label: 'Inventory', to: '/staff/inventory', icon: 'bi bi-box-seam' },
  { label: 'Reports', to: '/staff/reports', icon: 'bi bi-bar-chart' },
  { label: 'Stocktaking', to: '/staff/stocktaking', icon: 'bi bi-clipboard-check' },
]

const categories = INVENTORY_CATEGORIES
const formCategoryOptions = FORM_CATEGORY_OPTIONS

const searchQuery = ref('')
const selectedCategory = ref<CategoryFilter>('All')

/**
 * Empty by default.
 * Later should be replaced by backend-loaded data.
 */
const items = ref<InventoryItem[]>([])

const isEditMode = ref(false)
const form = ref<InventoryFormState>({ ...EMPTY_INVENTORY_FORM })

const inventoryModalRef = ref<InventoryModalExposed | null>(null)

/**
 * Reusable grouped stats object.
 * Keeps related numbers in one place.
 */
const inventoryStats = computed(() => ({
  totalItems: items.value.length,
  lowStockItems: items.value.filter(
      (item) => item.quantityAvailable <= item.lowStockThreshold
  ).length,
  reservedStock: items.value.reduce(
      (sum, item) => sum + item.quantityReserved,
      0
  ),
}))

const statsCards = computed<StatCardItem[]>(() => [
  {
    key: 'total-items',
    label: 'Total Items',
    value: inventoryStats.value.totalItems,
    icon: 'bi bi-box',
    iconColorClass: 'icon-blue',
  },
  {
    key: 'low-stock-items',
    label: 'Low Stock Items',
    value: inventoryStats.value.lowStockItems,
    icon: 'bi bi-exclamation-triangle',
    iconColorClass: 'icon-orange',
  },
  {
    key: 'reserved-stock',
    label: 'Reserved Stock',
    value: inventoryStats.value.reservedStock,
    icon: 'bi bi-arrow-up-right',
    iconColorClass: 'icon-purple',
  },
])

const availableUnits = computed(() => {
  return [...new Set(items.value.map((item) => item.unit).filter(Boolean))].sort()
})

const filteredItems = computed(() => {
  const normalizedSearch = searchQuery.value.trim().toLowerCase()

  return items.value.filter((item) => {
    const categoryMatches =
        selectedCategory.value === 'All' ||
        item.category === selectedCategory.value.toLowerCase()

    const searchMatches =
        item.name.toLowerCase().includes(normalizedSearch) ||
        item.category.toLowerCase().includes(normalizedSearch) ||
        item.unit.toLowerCase().includes(normalizedSearch)

    return categoryMatches && searchMatches
  })
})

function resetForm(): void {
  form.value = { ...EMPTY_INVENTORY_FORM }
}

function openCreateModal(): void {
  isEditMode.value = false
  resetForm()
  inventoryModalRef.value?.show()
}

function openEditModal(item: InventoryItem): void {
  isEditMode.value = true

  form.value = {
    id: item.id,
    name: item.name,
    category: item.category,
    unit: item.unit,
    quantityInStock: item.quantityInStock,
    quantityReserved: item.quantityReserved,
    quantityAvailable: item.quantityAvailable,
    lowStockThreshold: item.lowStockThreshold,
  }

  inventoryModalRef.value?.show()
}

function saveItem(): void {
  console.log('Saving item with form data:', form.value)
  if (isEditMode.value && form.value.id !== null) {
    const existingItem = items.value.find((item) => item.id === form.value.id)

    if (!existingItem) {
      return
    }

    existingItem.name = form.value.name
    existingItem.category = form.value.category
    existingItem.unit = form.value.unit
    existingItem.quantityInStock = form.value.quantityInStock
    existingItem.lowStockThreshold = form.value.lowStockThreshold
    updateItem(form.value.id, existingItem)
        .then(() => {
          fetchInventory();
        })
        .catch((error) => {
          console.error('Error updating item:', error)
        })

  } else {

    const newItem = {
      id: null,
      name: form.value.name,
      category: form.value.category,
      unit: form.value.unit,
      quantityInStock: form.value.quantityInStock,
      quantityReserved: 0,
      quantityAvailable: Math.max(0, form.value.quantityInStock),
      lowStockThreshold: form.value.lowStockThreshold,
    };

    addItem(newItem)
        .then(() => {
          fetchInventory();
        })
        .catch((error) => {
          console.error('Error updating item:', error)
        })
  }

  inventoryModalRef.value?.hide()
  resetForm();
}

function deleteInventoryItem(id: string | null): void {
  const shouldDelete = globalThis.confirm('Are you sure you want to delete this item?')

  if (!shouldDelete || !id) {
    return
  }

  deleteItem(id)
      .then(() => {
        fetchInventory();
      })
      .catch((error) => {
        console.error('Error updating item:', error)
      })
}

/**
 * Keep available quantity automatically synchronized in the form.
 */
watch(
    () => [form.value.quantityInStock, form.value.quantityReserved],
    () => {
      form.value.quantityAvailable = Math.max(
          0,
          Number(form.value.quantityInStock) - Number(form.value.quantityReserved)
      )
    },
    { immediate: true }
);

function fetchInventory(): void {
  getInventory()
      .then((data) => {
        items.value = data
        console.log('Inventory data loaded:', data)
      })
      .catch((error) => {
        console.error('Error fetching inventory:', error)
      })
}

onMounted(() => {
  fetchInventory();
});
</script>

<style scoped>
.inventory-view {
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

.icon-orange {
  color: #f97316;
}

.icon-purple {
  color: #a855f7;
}
</style>