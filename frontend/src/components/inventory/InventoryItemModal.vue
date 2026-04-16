<template>
  <div
      class="modal fade"
      id="inventoryItemModal"
      tabindex="-1"
      aria-labelledby="inventoryItemModalLabel"
      aria-hidden="true"
      ref="modalRoot"
  >
    <div class="modal-dialog modal-lg modal-dialog-centered">
      <div class="modal-content inventory-modal">
        <div class="modal-header border-0 pb-0">
          <div>
            <h5 class="modal-title fw-bold" id="inventoryItemModalLabel">
              {{ isEditMode ? 'Edit Inventory Item' : 'Add Inventory Item' }}
            </h5>
            <p class="text-secondary-emphasis mb-0 mt-1">
              Reserved and available values are visible but cannot be edited manually.
            </p>
          </div>

          <button
              type="button"
              class="btn-close"
              data-bs-dismiss="modal"
              aria-label="Close"
          ></button>
        </div>

        <div class="modal-body pt-4">
          <form class="row g-3" @submit.prevent="$emit('save')">
            <div class="col-12 col-md-6">
              <label class="form-label fw-semibold">Item Name</label>
              <input
                  :value="form.name"
                  type="text"
                  class="form-control rounded-4"
                  placeholder="e.g. Bath Towel"
                  required
                  @input="updateField('name', ($event.target as HTMLInputElement).value.trim())"
              />
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label fw-semibold">Category</label>
              <select
                  :value="form.category"
                  class="form-select rounded-4 text-capitalize"
                  required
                  @change="updateField('category', ($event.target as HTMLSelectElement).value)"
              >
                <option
                    v-for="option in formCategoryOptions"
                    :key="option"
                    :value="option"
                    class="text-capitalize"
                >
                  {{ option }}
                </option>
              </select>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label fw-semibold">Unit</label>
              <input
                  :value="form.unit"
                  type="text"
                  class="form-control rounded-4"
                  list="unitSuggestions"
                  placeholder="e.g. piece"
                  required
                  @input="updateField('unit', ($event.target as HTMLInputElement).value.trim())"
              />
              <datalist id="unitSuggestions">
                <option v-for="unit in availableUnits" :key="unit" :value="unit" />
              </datalist>
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label fw-semibold">Quantity In Stock</label>
              <input
                  :value="form.quantityInStock"
                  type="number"
                  min="0"
                  class="form-control rounded-4"
                  required
                  @input="updateField('quantityInStock', Number(($event.target as HTMLInputElement).value))"
              />
            </div>

            <div class="col-12 col-md-6">
              <label class="form-label fw-semibold">Low Stock Threshold</label>
              <input
                  :value="form.lowStockThreshold"
                  type="number"
                  min="0"
                  class="form-control rounded-4"
                  required
                  @input="updateField('lowStockThreshold', Number(($event.target as HTMLInputElement).value))"
              />
            </div>

            <div class="col-12 col-md-3">
              <label class="form-label fw-semibold">Reserved</label>
              <input
                  :value="form.quantityReserved"
                  type="number"
                  class="form-control rounded-4"
                  readonly
                  disabled
              />
            </div>

            <div class="col-12 col-md-3">
              <label class="form-label fw-semibold">Available</label>
              <input
                  :value="form.quantityAvailable"
                  type="number"
                  class="form-control rounded-4"
                  readonly
                  disabled
              />
            </div>

            <div class="col-12">
              <div class="d-flex justify-content-end gap-2 pt-2">
                <button
                    type="button"
                    class="btn btn-outline-secondary rounded-4 px-4"
                    data-bs-dismiss="modal"
                >
                  Cancel
                </button>

                <button
                    type="submit"
                    class="btn btn-primary rounded-4 px-4 fw-semibold"
                >
                  {{ isEditMode ? 'Save Changes' : 'Add Item' }}
                </button>
              </div>
            </div>
          </form>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import Modal from 'bootstrap/js/dist/modal'
import type { InventoryCategory, InventoryFormState } from '../../types/inventory'

/**
 * Inventory modal.
 *
 * Parent controls:
 * - current form object
 * - whether modal is in edit mode
 * - available unit suggestions
 * - available categories
 *
 * Emits:
 * - update:form
 * - save
 *
 * Exposes:
 * - show()
 * - hide()
 */

const props = defineProps<{
  form: InventoryFormState
  isEditMode: boolean
  availableUnits: string[]
  formCategoryOptions: InventoryCategory[]
}>()

const emit = defineEmits<{
  (e: 'update:form', value: InventoryFormState): void
  (e: 'save'): void
}>()

const modalRoot = ref<HTMLElement | null>(null)
let modalInstance: Modal | null = null

function updateField<K extends keyof InventoryFormState>(
    field: K,
    value: InventoryFormState[K]
): void {
  emit('update:form', {
    ...props.form,
    [field]: value,
  })
}

function show(): void {
  modalInstance?.show()
}

function hide(): void {
  modalInstance?.hide()
}

onMounted(() => {
  if (modalRoot.value) {
    modalInstance = new Modal(modalRoot.value)
  }
})

defineExpose({
  show,
  hide,
})
</script>

<style scoped>
.inventory-modal {
  background-color: var(--card-bg);
  color: var(--text-main);
  border: 1px solid var(--border-color);
  border-radius: 1.25rem;
}

.inventory-modal .form-control,
.inventory-modal .form-select {
  background-color: var(--page-bg);
  color: var(--text-main);
  border-color: var(--border-color);
}

.inventory-modal .form-control:disabled {
  opacity: 0.8;
  cursor: not-allowed;
}

.inventory-modal .btn-close {
  filter: var(--modal-close-filter, invert(1));
}
</style>