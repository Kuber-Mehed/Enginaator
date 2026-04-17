<template>
  <div class="page">
    <div class="flow-wrapper">
      <div class="flow-header">
        <div>
          <h2>System Flow</h2>
          <p>Backend sends data to both Database and AI. Database stores data, AI returns response to backend, then result goes to staff dashboard.</p>
        </div>
      </div>

      <v-chart class="chart" :option="option" autoresize />
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { use } from 'echarts/core'
import type { ComposeOption } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { GraphChart } from 'echarts/charts'
import type { GraphSeriesOption } from 'echarts/charts'
import { TooltipComponent } from 'echarts/components'
import type { TooltipComponentOption } from 'echarts/components'
import VChart from 'vue-echarts'

use([CanvasRenderer, GraphChart, TooltipComponent])

type EChartsOption = ComposeOption<
    GraphSeriesOption | TooltipComponentOption
>

const option = computed<EChartsOption>(() => ({
  backgroundColor: 'transparent',
  tooltip: {
    trigger: 'item',
    backgroundColor: '#111827',
    borderColor: '#374151',
    borderWidth: 1,
    textStyle: {
      color: '#e5e7eb',
      fontSize: 13
    },
    formatter(params: any) {
      if (params.dataType === 'node') {
        return params.data.name.replace('\n', '<br/>')
      }
      if (params.dataType === 'edge') {
        return params.data.label?.formatter || ''
      }
      return ''
    }
  },
  animationDuration: 1200,
  animationDurationUpdate: 700,
  series: [
    {
      type: 'graph',
      layout: 'none',
      roam: false,
      draggable: false,
      symbol: 'roundRect',
      symbolSize: [230, 78],
      edgeSymbol: ['none', 'arrow'],
      edgeSymbolSize: [0, 12],
      label: {
        show: true,
        color: '#f8fafc',
        fontSize: 17,
        fontWeight: 700,
        lineHeight: 24
      },
      itemStyle: {
        color: '#151a23',
        borderColor: '#5b6b86',
        borderWidth: 2,
        shadowBlur: 25,
        shadowColor: 'rgba(59, 130, 246, 0.18)',
        shadowOffsetY: 6
      },
      lineStyle: {
        color: '#8aa0bd',
        width: 3,
        opacity: 0.95
      },
      emphasis: {
        scale: true,
        itemStyle: {
          borderColor: '#93c5fd',
          borderWidth: 2.5,
          shadowBlur: 30,
          shadowColor: 'rgba(96, 165, 250, 0.28)'
        },
        lineStyle: {
          width: 4
        },
        label: {
          color: '#ffffff'
        }
      },
      data: [
        {
          id: 'guest',
          name: '👤 Guest\n(Frontend)',
          x: 620,
          y: 70,
          itemStyle: {
            color: '#141a22',
            borderColor: '#60a5fa',
            borderWidth: 2,
            shadowBlur: 28,
            shadowColor: 'rgba(96, 165, 250, 0.25)'
          }
        },
        {
          id: 'backend1',
          name: '⚙️ Backend',
          x: 620,
          y: 220,
          itemStyle: {
            color: '#161b24',
            borderColor: '#a78bfa',
            borderWidth: 2,
            shadowBlur: 28,
            shadowColor: 'rgba(167, 139, 250, 0.22)'
          }
        },
        {
          id: 'database',
          name: '🗄️ Database',
          x: 310,
          y: 380,
          itemStyle: {
            color: '#171c26',
            borderColor: '#22c55e',
            borderWidth: 2,
            shadowBlur: 30,
            shadowColor: 'rgba(34, 197, 94, 0.24)'
          }
        },
        {
          id: 'ai',
          name: '🧠 AI\n(made by us)',
          x: 930,
          y: 380,
          itemStyle: {
            color: '#171c26',
            borderColor: '#34d399',
            borderWidth: 2,
            shadowBlur: 30,
            shadowColor: 'rgba(52, 211, 153, 0.24)'
          }
        },
        {
          id: 'backend2',
          name: '⚙️ Backend',
          x: 930,
          y: 550,
          itemStyle: {
            color: '#161b24',
            borderColor: '#f59e0b',
            borderWidth: 2,
            shadowBlur: 28,
            shadowColor: 'rgba(245, 158, 11, 0.22)'
          }
        },
        {
          id: 'staff',
          name: '📊 Staff Dashboard\n(Frontend)',
          x: 620,
          y: 710,
          symbolSize: [255, 88],
          itemStyle: {
            color: '#141a22',
            borderColor: '#f472b6',
            borderWidth: 2,
            shadowBlur: 28,
            shadowColor: 'rgba(244, 114, 182, 0.24)'
          }
        }
      ],
      links: [
        {
          source: 'guest',
          target: 'backend1',
          lineStyle: {
            curveness: 0.05
          },
          label: {
            show: true,
            formatter: 'Request',
            color: '#cbd5e1',
            fontSize: 13,
            backgroundColor: 'rgba(17,24,39,0.85)',
            borderRadius: 8,
            padding: [4, 8]
          }
        },
        {
          source: 'backend1',
          target: 'database',
          lineStyle: {
            curveness: 0.12
          },
          label: {
            show: true,
            formatter: 'Store Data',
            color: '#cbd5e1',
            fontSize: 13,
            backgroundColor: 'rgba(17,24,39,0.85)',
            borderRadius: 8,
            padding: [4, 8]
          }
        },
        {
          source: 'backend1',
          target: 'ai',
          lineStyle: {
            curveness: -0.12
          },
          label: {
            show: true,
            formatter: 'Send to AI',
            color: '#cbd5e1',
            fontSize: 13,
            backgroundColor: 'rgba(17,24,39,0.85)',
            borderRadius: 8,
            padding: [4, 8]
          }
        },
        {
          source: 'ai',
          target: 'backend2',
          lineStyle: {
            curveness: 0.08
          },
          label: {
            show: true,
            formatter: 'AI Response',
            color: '#cbd5e1',
            fontSize: 13,
            backgroundColor: 'rgba(17,24,39,0.85)',
            borderRadius: 8,
            padding: [4, 8]
          }
        },
        {
          source: 'backend2',
          target: 'staff',
          lineStyle: {
            curveness: 0.08
          },
          label: {
            show: true,
            formatter: 'Show Result',
            color: '#cbd5e1',
            fontSize: 13,
            backgroundColor: 'rgba(17,24,39,0.85)',
            borderRadius: 8,
            padding: [4, 8]
          }
        }
      ]
    }
  ]
}))
</script>

<style scoped>
.page {
  width: 100vw;
  height: 100vh;
  overflow: hidden;
  background:
      radial-gradient(circle at top left, rgba(59, 130, 246, 0.2), transparent 28%),
      radial-gradient(circle at top right, rgba(168, 85, 247, 0.16), transparent 24%),
      radial-gradient(circle at bottom center, rgba(16, 185, 129, 0.14), transparent 30%),
      linear-gradient(180deg, #0b1020 0%, #0f172a 100%);
  display: flex;
  justify-content: center;
  align-items: center;
}

.flow-wrapper {
  width: 96vw;
  height: 94vh;
  padding: 20px;
  box-sizing: border-box;
  border-radius: 24px;
  background: rgba(15, 23, 42, 0.68);
  backdrop-filter: blur(12px);
  border: 1px solid rgba(148, 163, 184, 0.12);
  box-shadow:
      0 20px 60px rgba(0, 0, 0, 0.35),
      inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.flow-header {
  margin-bottom: 12px;
  padding: 6px 8px 0;
}

.flow-header h2 {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #f8fafc;
  letter-spacing: 0.2px;
}

.flow-header p {
  margin: 6px 0 0;
  font-size: 14px;
  color: #94a3b8;
  max-width: 760px;
}

.chart {
  width: 100%;
  height: calc(100% - 70px);
}
</style>