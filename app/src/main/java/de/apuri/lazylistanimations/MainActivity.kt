package de.apuri.lazylistanimations

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FractionalThreshold
import androidx.compose.material.rememberSwipeableState
import androidx.compose.material.swipeable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import de.apuri.lazylistanimations.ui.theme.LazyListAnimationsTheme
import kotlin.math.roundToInt

@ExperimentalFoundationApi
@ExperimentalMaterialApi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LazyListAnimationsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    var values by remember { mutableStateOf((1..10).toList()) }

                    Column {
                        AnimatableSwipeableLazyList(
                            modifier = Modifier.weight(1f),
                            values = values,
                            onRemove = { value ->
                                values = values.filter { it != value }
                            }
                        )
                        Button(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            onClick = { values = values.shuffled() }
                        ) {
                            Text(text = "Mix it up")
                        }
                    }
                }
            }
        }
    }
}


@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun AnimatableSwipeableLazyList(
    modifier: Modifier,
    values: List<Int>,
    onRemove: (Int) -> Unit
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(values, key = { it }) { value ->
            ListItem(
                value = value,
                onRemove = onRemove
            )
        }
    }
}

@ExperimentalFoundationApi
@ExperimentalMaterialApi
@Composable
fun LazyItemScope.ListItem(
    value: Int,
    onRemove: (Int) -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier.animateItemPlacement()
    ) {
        var swiped by remember { mutableStateOf(false) }

        val swipeableState = rememberSwipeableState(if (swiped) RIGHT else IDLE)

        if (swipeableState.targetValue == RIGHT && swipeableState.isAnimationRunning) {
            // We end up here after a fling/swipe which triggers the item to settle at position RIGHT.
            // `isAnimationRunning` is another way of saying "not dragging"
            LaunchedEffect(swipeableState.targetValue, swipeableState.isAnimationRunning) {
                swiped = true
            }
        }

        if (swipeableState.currentValue == RIGHT && !swipeableState.isAnimationRunning) {
            // The item settled at position RIGHT
            LaunchedEffect(swipeableState.currentValue, swipeableState.isAnimationRunning) {
                onRemove(value)
            }
        }

        val anchors = mapOf(
            0f to IDLE,
            with(LocalDensity.current) { maxWidth.toPx() } to RIGHT
        )

        AnimatedVisibility(
            modifier = Modifier.swipeable(
                state = swipeableState,
                anchors = anchors,
                orientation = Orientation.Horizontal,
                thresholds = { _, _ -> FractionalThreshold(0.5f) },
            ),
            visible = !swiped,
            exit = fadeOut(
                animationSpec = tween(300)
            ) + shrinkVertically(
                animationSpec = tween(150, 150),
                shrinkTowards = Alignment.CenterVertically
            )
        ) {
            // Bottom Layer
            if (swipeableState.offset.value > 0.0f) {
                Surface(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth(),
                    color = MaterialTheme.colorScheme.error,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        modifier = Modifier.padding(16.dp),
                        text = "DELETE"
                    )
                }

            }

            // Top Layer
            Surface(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .offset {
                        IntOffset(x = swipeableState.offset.value.roundToInt(), y = 0)
                    }
                    .fillMaxWidth(),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 4.dp,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    modifier = Modifier.padding(16.dp),
                    text = "$value"
                )
            }
        }
    }
}

private const val RIGHT = 1
private const val IDLE = 0