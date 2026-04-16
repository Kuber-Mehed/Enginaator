<template>
  <div class="d-flex flex-column justify-content-center align-items-center text-center py-5 empty-state">
    <i class="bi bi-box-seam display-4 mb-3 empty-state-icon"></i>
    <h3 class="fw-bold mb-2">No requests yet</h3>
    <p class="text-secondary-emphasis mb-0">
      Requests will appear here when guests make them
    </p>
  </div>
</template>

<script setup lang="ts">
</script>

<style scoped>
.empty-state {
  min-height: 260px;
}

.empty-state-icon {
  color: var(--empty-icon);
}
</style>