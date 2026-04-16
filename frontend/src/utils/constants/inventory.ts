import type { InventoryCategory, InventoryFormState } from '../types/inventory'

export const INVENTORY_CATEGORIES = [
    'All',
    'HOUSEKEEPING',
    'FOOD',
    'BEVERAGE',
    'AMENITY',
    'MAINTENANCE',
] as const

export type InventoryCategoryFilter = typeof INVENTORY_CATEGORIES[number]

export const FORM_CATEGORY_OPTIONS: InventoryCategory[] = [
    'HOUSEKEEPING',
    'FOOD',
    'BEVERAGE',
    'AMENITY',
    'MAINTENANCE',
]

export const EMPTY_INVENTORY_FORM: InventoryFormState = {
    id: null,
    name: '',
    category: 'HOUSEKEEPING',
    unit: 'piece',
    quantityInStock: 0,
    quantityReserved: 0,
    quantityAvailable: 0,
    lowStockThreshold: 0,
}