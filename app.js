const themeToggle = document.querySelector("#themeToggle");
const addTask = document.querySelector("#addTask");
const taskList = document.querySelector("#taskList");
const taskValue = document.querySelector("#taskValue");
const progressValue = document.querySelector("#progressValue");

let taskCount = 12;
let progress = 68;

themeToggle.addEventListener("click", () => {
  document.body.classList.toggle("dark");
});

addTask.addEventListener("click", () => {
  taskCount += 1;
  progress = Math.min(100, progress + 3);

  const item = document.createElement("li");
  item.innerHTML = '<span class="checkmark pending">•</span><span>新任务已加入</span>';
  taskList.append(item);

  taskValue.textContent = String(taskCount);
  progressValue.textContent = `${progress}%`;
});
