<template>
  <section class="card dashboard-card border-0 shadow-sm rounded-4 overflow-hidden">
    <div class="table-responsive">
      <table class="table inventory-table align-middle mb-0">
        <thead>
        <tr>
          <th>Item</th>
          <th>Category</th>
          <th>In Stock</th>
          <th>Reserved</th>
          <th>Available</th>
          <th>Threshold</th>
          <th class="text-end">Actions</th>
        </tr>
        </thead>

        <tbody>
        <tr v-if="items.length === 0">
          <td colspan="7">
            <div class="py-5 text-center">
              <i class="bi bi-box-seam display-6 empty-icon mb-3 d-block"></i>
              <h5 class="fw-bold mb-2">No inventory items found</h5>
              <p class="text-secondary-emphasis mb-0">
                Items will appear here when loaded from backend or added manually
              </p>
            </div>
          </td>
        </tr>

        <tr v-for="item in items" :key="item.id!">
          <td>
            <div class="d-flex align-items-center gap-3">
              <div class="item-icon d-flex align-items-center justify-content-center rounded-4">
                <i class="bi bi-box-seam"></i>
              </div>

              <div>
                <div class="fw-bold fs-5">{{ item.name }}</div>
                <div class="small text-secondary-emphasis text-capitalize">
                  {{ item.unit }}
                </div>
              </div>
            </div>
          </td>

          <td>
              <span class="badge rounded-pill category-badge text-capitalize px-3 py-2">
                {{ item.category }}
              </span>
          </td>

          <td class="fw-semibold">{{ item.quantityInStock }}</td>
          <td>{{ item.quantityReserved }}</td>
          <td class="fw-bold text-success">{{ item.quantityAvailable }}</td>
          <td>{{ item.lowStockThreshold }}</td>

          <td class="text-end">
            <div class="dropdown">
              <button
                  class="btn btn-action-menu rounded-4"
                  type="button"
                  data-bs-toggle="dropdown"
                  aria-expanded="false"
              >
                <i class="bi bi-three-dots"></i>
              </button>

              <ul class="dropdown-menu dropdown-menu-end rounded-4 shadow-sm">
                <li>
                  <button
                      class="dropdown-item d-flex align-items-center gap-2"
                      type="button"
                      @click="$emit('edit', item)"
                  >
                    <i class="bi bi-pencil-square"></i>
                    Edit item
                  </button>
                </li>
                <li>
                  <button
                      class="dropdown-item d-flex align-items-center gap-2 text-danger"
                      type="button"
                      @click="$emit('delete', item.id)"
                  >
                    <i class="bi bi-trash"></i>
                    Delete item
                  </button>
                </li>
              </ul>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
    </div>
  </section>
</template>

<script setup lang="ts">
import type { InventoryItem } from '@/utils/types/inventory.ts'

/**
 * Inventory table.
 *
 * Props:
 * - items: already filtered item list
 *
 * Emits:
 * - edit(item)
 * - delete(id)
 */

defineProps<{
  items: InventoryItem[]
}>()

defineEmits<{
  (e: 'edit', item: InventoryItem): void
  (e: 'delete', id: string | null): void
}>()
</script>

<style scoped>
.dashboard-card {
  background-color: var(--card-bg);
  color: var(--text-main);
}

.inventory-table {
  --bs-table-bg: transparent;
  --bs-table-color: var(--text-main);
  --bs-table-border-color: var(--border-color);
  margin-bottom: 0;
}

.inventory-table thead th {
  color: var(--text-muted);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.03em;
  padding: 1.25rem 1.5rem;
  background-color: rgba(148, 163, 184, 0.06);
  border-bottom-width: 1px;
}

.inventory-table tbody td {
  padding: 1.25rem 1.5rem;
}

.item-icon {
  width: 46px;
  height: 46px;
  background-color: rgba(37, 99, 235, 0.12);
  color: var(--primary);
}

.category-badge {
  background-color: rgba(168, 85, 247, 0.2);
  color: #d8b4fe;
  font-weight: 600;
}

.btn-action-menu {
  background-color: rgba(148, 163, 184, 0.08);
  color: var(--text-main);
  border: none;
  width: 42px;
  height: 42px;
}

.btn-action-menu:hover {
  background-color: rgba(148, 163, 184, 0.16);
}

.empty-icon {
  color: var(--empty-icon);
}

@media (max-width: 991.98px) {
  .inventory-table thead th,
  .inventory-table tbody td {
    padding: 1rem;
  }
}
</style>