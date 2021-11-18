This is a dirty POC of a LazyColumn that supports swipe-to-dismiss and reordering.
This solution is dirty because items are only really removed from the list when the removal animation has finished.
Even worse, we are not really waiting for the animation to end, coincidentally it takes about the same time that the swipe animation takes to settle.

https://user-images.githubusercontent.com/1836066/142501293-6f90519b-0c4a-438c-8fc5-a6b4259f5b52.mp4

## Todos
- Add insert animations
- Support RTL
- Hoist the item states (entering, idle, exiting). A swipe should immediately call `onRemove`, after that the item should still be part of the list until it's gone (Hoisting the animation duration vs MutableTransitionState?)
