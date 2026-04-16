<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup lang="ts">

import {onMounted} from "vue";
import {STAFF_TOPIC, stompClient} from "@/services/websocket.ts";

onMounted(() => {
  console.log("App mounted");
  stompClient.activate();
})
</script>

<style>
#app {
  min-height: 100vh;
}
</style>