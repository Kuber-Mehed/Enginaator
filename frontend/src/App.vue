<template>
  <h1>You did it!</h1>
  <p>
    Visit <a href="https://vuejs.org/" target="_blank" rel="noopener">vuejs.org</a> to read the
    documentation
  </p>
</template>

<script setup lang="ts">

import {onMounted} from "vue";
import {STAFF_TOPIC, stompClient} from "@/services/websocket.ts";

onMounted(() => {
  console.log("App mounted");
  stompClient.activate();
})
</script>

<style scoped></style>
