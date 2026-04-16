<template>
  <div class="card dashboard-card border-0 h-100 shadow-sm rounded-4">
    <div class="card-body p-4">
      <div class="d-flex justify-content-between align-items-start mb-3">
        <span class="text-secondary fw-semibold">{{ label }}</span>
        <i :class="[icon, iconColorClass, 'fs-5']"></i>
      </div>
      <div class="display-6 fw-bold">{{ value }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
defineProps<{
  label: string
  value: number
  icon: string
  iconColorClass: string
}>()
</script>

<style scoped>
.dashboard-card {
  background-color: var(--card-bg);
  color: var(--text-main);
}
</style>