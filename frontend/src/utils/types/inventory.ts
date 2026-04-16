export type InventoryCategory =
    | 'HOUSEKEEPING'
    | 'FOOD'
    | 'BEVERAGE'
    | 'AMENITY'
    | 'MAINTENANCE'

export interface InventoryItem {
    id: string | null
    name: string
    category: InventoryCategory
    unit: string
    quantityInStock: number
    quantityReserved: number
    quantityAvailable: number
    lowStockThreshold: number
}

export interface InventoryFormState {
    id: string | null
    name: string
    category: InventoryCategory
    unit: string
    quantityInStock: number
    quantityReserved: number
    quantityAvailable: number
    lowStockThreshold: number
}