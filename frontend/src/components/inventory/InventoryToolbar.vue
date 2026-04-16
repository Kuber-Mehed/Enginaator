<template>
  <section class="card dashboard-card border-0 shadow-sm rounded-4 mb-4">
    <div class="card-body p-4">
      <div
          class="d-flex flex-column flex-xl-row gap-3 align-items-stretch align-items-xl-center"
      >
        <div class="search-box flex-grow-1">
          <div class="input-group input-group-lg">
            <span class="input-group-text search-addon border-end-0 rounded-start-4">
              <i class="bi bi-search"></i>
            </span>

            <input
                :value="searchQuery"
                type="text"
                class="form-control border-start-0 rounded-end-4 search-input"
                placeholder="Search inventory..."
                @input="handleSearchInput"
            />
          </div>
        </div>

        <div class="d-flex flex-wrap gap-2">
          <button
              v-for="category in categories"
              :key="category"
              type="button"
              class="btn rounded-4 px-3 py-2 fw-semibold"
              :class="selectedCategory === category ? 'btn-primary' : 'btn-filter'"
              @click="emit('update:selectedCategory', category)"
          >
            {{ category }}
          </button>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
type CategoryFilter =
    | 'All'
    | 'Housekeeping'
    | 'Food'
    | 'Beverage'
    | 'Amenity'
    | 'Maintenance'

defineProps<{
  searchQuery: string
  selectedCategory: CategoryFilter
  categories: readonly CategoryFilter[]
}>()

const emit = defineEmits<{
  (e: 'update:searchQuery', value: string): void
  (e: 'update:selectedCategory', value: CategoryFilter): void
}>()

function handleSearchInput(event: Event): void {
  const target = event.target as HTMLInputElement
  emit('update:searchQuery', target.value)
}
</script>

<style scoped>
.dashboard-card {
  background-color: var(--card-bg);
  color: var(--text-main);
}

.search-addon,
.search-input {
  background-color: var(--card-bg);
  color: var(--text-main);
  border-color: var(--border-color);
}

.search-input::placeholder {
  color: var(--text-muted);
}

.btn-filter {
  background-color: var(--segment-bg);
  color: var(--segment-color);
  border: 1px solid transparent;
}

.btn-filter:hover {
  color: var(--text-main);
  border-color: var(--border-color);
}
</style>