<template>
  <button
      type="button"
      class="btn btn-link sidebar-theme-toggle text-start text-decoration-none d-flex align-items-center gap-3 rounded-4 px-3 py-3 w-100"
      @click="$emit('toggle')"
  >
    <i :class="theme === 'dark' ? 'bi bi-sun' : 'bi bi-moon'"></i>
    <span class="fw-semibold">
      {{ theme === 'dark' ? 'Light Mode' : 'Dark Mode' }}
    </span>
  </button>
</template>

<script setup lang="ts">
defineProps<{
  theme: 'dark' | 'light'
}>()

defineEmits<{
  (e: 'toggle'): void
}>()
</script>

<style scoped>
.sidebar-theme-toggle {
  color: var(--text-main);
  background-color: var(--card-bg);
  border: 1px solid var(--border-color);
  transition: background-color 0.2s ease, color 0.2s ease, border-color 0.2s ease;
}

.sidebar-theme-toggle:hover {
  background-color: var(--link-hover-bg);
}
</style>
