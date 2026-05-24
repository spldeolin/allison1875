<script setup lang="ts">
import { NMessageProvider, NConfigProvider, zhCN, dateZhCN, type GlobalThemeOverrides } from 'naive-ui'

const themeOverrides: GlobalThemeOverrides = {
  common: {
    borderRadius: '8px',
    borderRadiusSmall: '6px',
    fontFamily: "'Plus Jakarta Sans', -apple-system, BlinkMacSystemFont, 'PingFang SC', 'Hiragino Sans GB', 'Microsoft YaHei', sans-serif",
    fontSize: '14px',
    heightMedium: '36px'
  },
  Button: {
    borderRadiusMedium: '8px',
    borderRadiusSmall: '6px',
    fontWeightStrong: '600'
  },
  Card: {
    borderRadius: '12px',
    paddingMedium: '20px',
    borderColor: '#E5E7EB'
  },
  Input: {
    borderRadius: '8px',
    border: '1px solid #E5E7EB'
  },
  DataTable: {
    thColor: '#F9FAFB',
    thTextColor: '#64748B',
    thFontWeight: '600',
    tdColor: '#FFFFFF',
    borderRadius: '12px',
    borderColor: '#F1F5F9'
  },
  Menu: {
    borderRadius: '8px',
    itemTextColor: '#475569',
    itemColorHover: '#F8FAFC'
  },
  Modal: {
    borderRadius: '16px'
  }
}
</script>

<template>
  <NConfigProvider :locale="zhCN" :date-locale="dateZhCN" :theme-overrides="themeOverrides">
    <NMessageProvider>
      <router-view />
    </NMessageProvider>
  </NConfigProvider>
</template>
