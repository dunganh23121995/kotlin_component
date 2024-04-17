package jp.co.spiderplus.transformation.transformation_item

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.window.Popup

class PositionRectangle(
    var top: Float = 0f,
    var left: Float = 0f,
    var bottom: Float = 0f,
    var right: Float = 0f,
) {
    fun includePoint(point: Offset): Boolean {
        return point.x > left && point.x < right && point.y > top && point.y < bottom
    }

    override fun toString(): String {
        return "left:$left top:$top right:$right bottom:$bottom ";
    }
}

@Composable
fun TransformationItem(
    mapCheckHoverItem: MutableMap<String, PositionRectangle>,
    key: String,
    hoverToOtherContent: @Composable (key: String?) -> Unit = {},
    longPressPopUpContent: @Composable (widthItem: Float, heighItem: Float) -> Unit = { _, _ -> },
    content: @Composable () -> Unit
) {
    val enablePopOver = remember {
        mutableStateOf(false)
    }
    val positionPopOver = remember {
        mutableStateOf(Offset.Zero)
    }
    val isOverToOtherItem = remember {
        mutableStateOf(false)
    }
    val positionItem = remember {
        mutableStateOf(Offset.Zero)
    }
    val itemSize = remember {
        mutableStateOf(IntSize(0, 0))
    }
    val keyOverItem = remember {
        mutableStateOf("")
    }
    Box(
        modifier = Modifier
            .onGloballyPositioned(
                onGloballyPositioned = { layoutCoordinates ->
                    positionItem.value =
                        layoutCoordinates.positionInRoot()
                    itemSize.value = layoutCoordinates.size / 2
                    if (mapCheckHoverItem[key] == null) {
                        mapCheckHoverItem[key] = PositionRectangle(
                            top = positionItem.value.y,
                            left = positionItem.value.x,
                            bottom = positionItem.value.y + itemSize.value.height,
                            right = positionItem.value.x + itemSize.value.width
                        )
                    } else {

                        mapCheckHoverItem[key]!!.top = positionItem.value.y
                        mapCheckHoverItem[key]!!.left = positionItem.value.x
                        mapCheckHoverItem[key]!!.bottom =
                            positionItem.value.y + itemSize.value.height
                        mapCheckHoverItem[key]!!.right =
                            positionItem.value.x + itemSize.value.width
                    }

                }
            )
            .pointerInput(
                key1 = key,
                block = {
                    detectTapGestures(
                        onLongPress = { offset ->
                            enablePopOver.value = true
                            positionPopOver.value = Offset.Zero
                        }
                    )
                }
            )
            .pointerInput(key1 = key,
                block = {
                    detectDragGesturesAfterLongPress(
                        onDragCancel = {
                            enablePopOver.value = false
                        },
                        onDragEnd = {
                            enablePopOver.value = false
                        },
                        onDrag = { _, offset ->
                            positionPopOver.value += offset
                            isOverToOtherItem.value =
                                mapCheckHoverItem.values.any {
                                    return@any it.includePoint(
                                        positionItem.value + Offset(
                                            x = (itemSize.value.width / 2).toFloat(),
                                            y = (itemSize.value.height / 2).toFloat()
                                        ) + positionPopOver.value
                                    )
                                }
                            keyOverItem.value = mapCheckHoverItem.keys.firstOrNull {
                                mapCheckHoverItem[it]?.includePoint(
                                    positionItem.value + Offset(
                                        x = (itemSize.value.width / 2).toFloat(),
                                        y = (itemSize.value.height / 2).toFloat()
                                    ) + positionPopOver.value
                                ) ?: false
                            } ?: ""
                        }
                    )
                }
            )
    ) {
        if (enablePopOver.value)
            Popup(
                offset = IntOffset(
                    x = positionPopOver.value.x.toInt(),
                    y = positionPopOver.value.y.toInt()
                ),
            ) {
                // double item ở đây để có thể tạo pop over. Nhớ opacity 0.5
                Box {
                    longPressPopUpContent.invoke(
                        itemSize.value.width.toFloat(),
                        itemSize.value.height.toFloat()
                    )
                    if (isOverToOtherItem.value)
                        hoverToOtherContent.invoke(keyOverItem.value)
                }
            }
        content.invoke()
    }
}