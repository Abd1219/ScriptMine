package com.abdapps.scriptmine.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// Futuristic glass effect modifier
fun Modifier.futuristicGlass(
    cornerRadius: Dp = 16.dp,
    glowColor: Color = BorderGlow,
    isPressed: Boolean = false
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .background(
            brush = Brush.linearGradient(
                colors = listOf(
                    GlassOverlay,
                    Color.Transparent,
                    GlassOverlay.copy(alpha = 0.1f)
                )
            ),
            shape = shape
        )
        .border(
            width = if (isPressed) 2.dp else 1.dp,
            brush = Brush.linearGradient(
                colors = listOf(
                    glowColor,
                    glowColor.copy(alpha = 0.3f),
                    glowColor
                )
            ),
            shape = shape
        )
        .clip(shape)
}

// Futuristic elevated surface with neon glow
fun Modifier.futuristicElevated(
    cornerRadius: Dp = 16.dp,
    glowColor: Color = BorderGlow,
    isPressed: Boolean = false
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .shadow(
            elevation = if (isPressed) 8.dp else 16.dp,
            shape = shape,
            ambientColor = glowColor.copy(alpha = 0.3f),
            spotColor = glowColor.copy(alpha = 0.5f)
        )
        .background(
            color = if (isPressed) SurfaceCard.copy(alpha = 0.8f) else SurfaceCard,
            shape = shape
        )
        .border(
            width = if (isPressed) 2.dp else 1.dp,
            color = glowColor.copy(alpha = if (isPressed) 0.8f else 0.6f),
            shape = shape
        )
        .clip(shape)
}

// Futuristic input field with inner glow
fun Modifier.futuristicInput(
    cornerRadius: Dp = 12.dp,
    glowColor: Color = BorderGlow,
    isFocused: Boolean = false
): Modifier {
    val shape = RoundedCornerShape(cornerRadius)
    return this
        .background(
            color = SurfaceInput,
            shape = shape
        )
        .border(
            width = if (isFocused) 2.dp else 1.dp,
            color = if (isFocused) glowColor else glowColor.copy(alpha = 0.3f),
            shape = shape
        )
        .clip(shape)
}

@Composable
fun FuturisticButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    isPrimary: Boolean = true,
    cornerRadius: Dp = 12.dp,
    contentPadding: PaddingValues = PaddingValues(horizontal = 24.dp, vertical = 16.dp),
    content: @Composable RowScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val interactionSource = remember { MutableInteractionSource() }
    
    // Animated glow effect
    val infiniteTransition = rememberInfiniteTransition(label = "glow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Box(
        modifier = modifier
            .futuristicElevated(
                cornerRadius = cornerRadius,
                glowColor = if (isPrimary) Primary else BorderSecondary,
                isPressed = isPressed
            )
            .then(
                if (isPrimary) {
                    Modifier.background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                PrimaryGradientStart.copy(alpha = glowAlpha),
                                PrimaryGradientEnd.copy(alpha = glowAlpha)
                            )
                        ),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                } else {
                    Modifier.background(
                        color = SurfaceElevated.copy(alpha = 0.7f),
                        shape = RoundedCornerShape(cornerRadius)
                    )
                }
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = enabled
            ) {
                isPressed = true
                onClick()
                isPressed = false
            }
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content
        )
    }
}

@Composable
fun FuturisticCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 16.dp,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    onClick: (() -> Unit)? = null,
    glowColor: Color = BorderGlow,
    content: @Composable ColumnScope.() -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    var isHovered by remember { mutableStateOf(false) }
    
    // Animated border glow
    val infiniteTransition = rememberInfiniteTransition(label = "cardGlow")
    val borderAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "borderAlpha"
    )
    
    Box(
        modifier = modifier
            .futuristicElevated(
                cornerRadius = cornerRadius,
                glowColor = glowColor.copy(alpha = borderAlpha),
                isPressed = isPressed
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        isPressed = true
                        onClick()
                        isPressed = false
                    }
                } else {
                    Modifier
                }
            )
            .padding(contentPadding)
    ) {
        Column(content = content)
    }
}

@Composable
fun FuturisticTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else Int.MAX_VALUE,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    keyboardActions: KeyboardActions = KeyboardActions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    isError: Boolean = false
) {
    var isFocused by remember { mutableStateOf(false) }
    
    // Animated glow for focused state
    val focusedAlpha by animateFloatAsState(
        targetValue = if (isFocused) 1f else 0.3f,
        animationSpec = tween(300),
        label = "focusedAlpha"
    )
    
    val glowColor = when {
        isError -> ErrorColor
        isFocused -> Primary
        else -> BorderGlow
    }
    
    Box(
        modifier = modifier
            .futuristicInput(
                glowColor = glowColor.copy(alpha = focusedAlpha),
                isFocused = isFocused
            )
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            textStyle = TextStyle(
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal
            ),
            singleLine = singleLine,
            maxLines = maxLines,
            keyboardOptions = keyboardOptions,
            keyboardActions = keyboardActions,
            visualTransformation = visualTransformation,
            onTextLayout = { isFocused = true },
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        color = TextPlaceholder,
                        fontSize = 16.sp
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
fun FuturisticIconButton(
    onClick: () -> Unit,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    size: Dp = 48.dp,
    iconSize: Dp = 24.dp,
    glowColor: Color = Primary,
    contentDescription: String? = null
) {
    var isPressed by remember { mutableStateOf(false) }
    
    // Pulsing glow animation
    val infiniteTransition = rememberInfiniteTransition(label = "iconGlow")
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    
    Box(
        modifier = modifier
            .size(size)
            .futuristicElevated(
                cornerRadius = size / 2,
                glowColor = glowColor.copy(alpha = glowAlpha),
                isPressed = isPressed
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                isPressed = true
                onClick()
                isPressed = false
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(iconSize),
            tint = glowColor
        )
    }
}

@Composable
fun FuturisticToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val toggleWidth = 56.dp
    val toggleHeight = 32.dp
    val thumbSize = 24.dp
    
    // Animated position and color
    val thumbPosition by animateFloatAsState(
        targetValue = if (checked) 1f else 0f,
        animationSpec = tween(300, easing = EaseInOutCubic),
        label = "thumbPosition"
    )
    
    val glowColor = if (checked) NeonGreen else TextSecondary
    
    Box(
        modifier = modifier
            .size(width = toggleWidth, height = toggleHeight)
            .futuristicInput(
                cornerRadius = toggleHeight / 2,
                glowColor = glowColor,
                isFocused = checked
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onCheckedChange(!checked) }
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(thumbSize)
                .offset(x = (toggleWidth - toggleHeight) * thumbPosition)
                .futuristicElevated(
                    cornerRadius = thumbSize / 2,
                    glowColor = glowColor
                )
                .background(
                    brush = if (checked) {
                        Brush.radialGradient(
                            colors = listOf(NeonGreen, NeonGreen.copy(alpha = 0.7f))
                        )
                    } else {
                        Brush.radialGradient(
                            colors = listOf(TextSecondary, TextSecondary.copy(alpha = 0.7f))
                        )
                    },
                    shape = RoundedCornerShape(thumbSize / 2)
                )
        )
    }
}