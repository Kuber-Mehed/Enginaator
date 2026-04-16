export type DiscrepancyReason = 'DAMAGED' | 'THEFT' | 'MISCOUNTED' | 'SUPPLIER_ERROR'

export interface StocktakingSessionItem {
  id: string
  name: string
  category: string
  unit: string
  expectedCount: number
}

export interface StocktakingWorkingItem extends StocktakingSessionItem {
  physicalCount: number
  reason: DiscrepancyReason | null
}

export interface ReconciliationItemView {
  itemId: string
  itemName: string
  expectedCount: number
  physicalCount: number
  discrepancy: number
  reason: DiscrepancyReason | null
}

export interface ReconciliationView {
  id: string
  createdAt: string
  items: ReconciliationItemView[]
  totalDiscrepancies: number
}
