<template>
  <div class="page-container">
    <SearchForm
      :schema="props.schema"
      :model="searchModel"
      @search="handleSearch"
      @reset="handleReset"
    />
    <DataTable
      :schema="props.schema"
      :data="tableData"
      :loading="loading"
      :pagination="pagination"
      :permissions="props.permissions"
      :hide-create-button="true"
      :hide-row-actions="true"
      @page-change="handlePageChange"
    />
  </div>
</template>

<script setup>
import { defineProps } from 'vue'
import { useCrudPage } from '@/core/composables/useCrudPage'
import SearchForm from '@/core/SearchForm.vue'
import DataTable from '@/core/DataTable.vue'

const props = defineProps({
  schema: { type: Object, required: true },
  permissions: { type: Object, default: () => ({}) }
})

const {
  searchModel,
  tableData,
  loading,
  pagination,
  handleSearch,
  handleReset,
  handlePageChange
} = useCrudPage(props.schema)
</script>
