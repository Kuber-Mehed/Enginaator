<template>
  <header
      class="d-flex flex-column flex-lg-row justify-content-between align-items-start align-items-lg-center gap-3 mb-4 mb-xl-5"
  >
    <div>
      <h1 class="display-5 fw-bold mb-2">Inventory Management</h1>
      <p class="text-secondary-emphasis mb-0">
        Monitor stock levels and manage inventory
      </p>
    </div>

    <button
        type="button"
        class="btn btn-primary rounded-4 px-4 py-2 fw-semibold"
        @click="$emit('add-item')"
    >
      <i class="bi bi-plus-lg me-2"></i>
      Add Item
    </button>
  </header>
</template>

<script setup lang="ts">
defineEmits<{
  (e: 'add-item'): void
}>()
</script>