<template>
  <div class="statistics-page">
    <div class="filters">
      <input v-model="filters.guard" placeholder="Охранник" />
      <input v-model="filters.dateFrom" type="datetime-local" />
      <input v-model="filters.dateTo" type="datetime-local" />
      <button @click="loadData">Применить фильтры</button>
      <button @click="exportToExcel">Экспорт Excel</button>
      <button @click="exportToCsv">Экспорт CSV</button>
    </div>

    <div class="charts">
      <Line :data="violationsChart" :options="chartOptions" />
      <Bar :data="checkpointsChart" :options="chartOptions" />
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { Line, Bar } from 'vue-chartjs'
import { Chart, CategoryScale, LinearScale, PointElement, LineElement, BarElement } from 'chart.js'
import * as XLSX from 'xlsx'
import axios from 'axios'

Chart.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement)

const filters = ref({ guard: '', dateFrom: '', dateTo: '' })
const violationsData = ref([])
const checkpointsData = ref([])

const loadData = async () => {
  const params = {
    from: filters.value.dateFrom,
    to: filters.value.dateTo
  }
  violationsData.value = await axios.get('/api/statistics/violations', { params })
  checkpointsData.value = await axios.get('/api/statistics/checkpoints', { params })
}

const violationsChart = computed(() => ({
  labels: violationsData.value.map(v => v.type),
  datasets: [{ label: 'Нарушения', data: violationsData.value.map(v => v.count) }]
}))

const checkpointsChart = computed(() => ({
  labels: checkpointsData.value.map(c => c.checkpointName),
  datasets: [{ label: 'Время прохождения', data: checkpointsData.value.map(c => c.avgTime) }]
}))

const exportToExcel = () => {
  const ws = XLSX.utils.json_to_sheet(violationsData.value)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, 'Нарушения')
  XLSX.writeFile(wb, 'violations.xlsx')
}

const exportToCsv = () => {
  const ws = XLSX.utils.json_to_sheet(violationsData.value)
  const csv = XLSX.utils.sheet_to_csv(ws)
  const blob = new Blob([csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = 'violations.csv'
  link.click()
}
</script>