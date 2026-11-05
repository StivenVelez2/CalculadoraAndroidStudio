package com.example.calculadora

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController

private const val SHRINK_FACTOR = 0.9f
private val DefaultIconSize = 30.dp
private val ButtonTextSize = 36.sp
private val KeySpacing = 12.dp

@Composable
fun ShowCalculator(
    viewModel: CalculatorViewModel = viewModel(),
    navController: NavController
) {
    val result = viewModel.result.value
    val expression = viewModel.expression.value
    val error = viewModel.error.value

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.calculator_background))
            .systemBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(KeySpacing)
    ) {
        CalculatorIconButton(
            iconRes = R.drawable.ic_history_calculator,
            contentDescription = stringResource(R.string.content_description_history),
            backgroundColor = colorResource(R.color.calculator_button_digit),
            contentColor = colorResource(R.color.calculator_text_primary),
            onClick = { navController.navigate("history") },
            modifier = Modifier.size(44.dp),
            iconSize = 22.dp
        )
        CalculatorDisplay(
            expression = expression,
            result = result,
            error = error,
            modifier = Modifier.weight(1f)
        )
        CalculatorKeypad(viewModel = viewModel)
    }
}

@Composable
fun RowScope.ShowButton(text: String, color: Color, viewModel: CalculatorViewModel) {
    val selected = viewModel.activeOperation.value == text
    val backgroundColor = if (selected) {
        colorResource(R.color.calculator_button_operator_selected)
    } else {
        color
    }
    val textColor = if (selected) {
        colorResource(R.color.calculator_button_operator)
    } else {
        colorResource(R.color.calculator_text_primary)
    }

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(role = Role.Button) { viewModel.manageClickButtons(text) },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = ButtonTextSize
        )
    }
}

@Composable
private fun CalculatorDisplay(
    expression: String,
    result: String,
    error: CalculatorError?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.End
    ) {
        AutoSizeText(
            text = if (error != null) stringResource(error.messageRes) else expression,
            color = colorResource(R.color.calculator_text_secondary),
            maxFontSize = 28.sp,
            minFontSize = 14.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        AutoSizeText(
            text = if (error != null) stringResource(R.string.error_title) else formatNumber(result),
            color = colorResource(R.color.calculator_text_primary),
            maxFontSize = 72.sp,
            minFontSize = 28.sp,
            fontWeight = FontWeight.Light
        )
    }
}

@Composable
private fun CalculatorKeypad(viewModel: CalculatorViewModel, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(KeySpacing)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KeySpacing)
        ) {
            ShowButton(
                stringResource(R.string.button_delete),
                colorResource(R.color.calculator_button_function),
                viewModel
            )
            ShowButton(
                stringResource(R.string.button_all_clear),
                colorResource(R.color.calculator_button_function),
                viewModel
            )
            ShowButton(
                stringResource(R.string.button_percent),
                colorResource(R.color.calculator_button_function),
                viewModel
            )
            ShowButton(
                stringResource(R.string.button_divide),
                colorResource(R.color.calculator_button_operator),
                viewModel
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KeySpacing)
        ) {
            ShowButton(
                stringResource(R.string.button_digit_7),
                colorResource(R.color.calculator_button_digit),
                viewModel
            )
            ShowButton(
                stringResource(R.string.button_digit_8),
                colorResource(R.color.calculator_button_digit),
                viewModel
            )
            ShowButton(
                stringResource(R.string.button_digit_9),
                colorResource(R.color.calculator_button_digit),
                viewModel
            )
            ShowButton(
                stringResource(R.string.button_multiply),
                colorResource(R.color.calculator_button_operator),
                viewModel
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KeySpacing)
        ) {
            ShowButton(
                stringResource(R.string.button_digit_4),
                colorResource(R.color.calculator_button_digit),
                viewModel
            )
            ShowButton(
                stringResource(R.string.button_digit_5),
                colorResource(R.color.calculator_button_digit),
                viewModel
            )
            ShowButton(
                stringResource(R.string.button_digit_6),
                colorResource(R.color.calculator_button_digit),
                viewModel
            )
            ShowButton(
                stringResource(R.string.button_subtract),
                colorResource(R.color.calculator_button_operator),
                viewModel
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KeySpacing)
        ) {
            ShowButton(
                stringResource(R.string.button_digit_1),
                colorResource(R.color.calculator_button_digit),
                viewModel
            )
            ShowButton(
                stringResource(R.string.button_digit_2),
                colorResource(R.color.calculator_button_digit),
                viewModel
            )
            ShowButton(
                stringResource(R.string.button_digit_3),
                colorResource(R.color.calculator_button_digit),
                viewModel
            )
            ShowButton(
                stringResource(R.string.button_add),
                colorResource(R.color.calculator_button_operator),
                viewModel
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(KeySpacing)
        ) {
            ShowButton(
                stringResource(R.string.button_toggle_sign),
                colorResource(R.color.calculator_button_digit),
                viewModel
            )
            ShowButton(
                stringResource(R.string.button_digit_0),
                colorResource(R.color.calculator_button_digit),
                viewModel
            )
            ShowButton(
                stringResource(R.string.button_decimal),
                colorResource(R.color.calculator_button_digit),
                viewModel
            )
            ShowButton(
                stringResource(R.string.button_equals),
                colorResource(R.color.calculator_button_operator),
                viewModel
            )
        }
    }
}

@Composable
internal fun CalculatorIconButton(
    @DrawableRes iconRes: Int,
    contentDescription: String,
    backgroundColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    iconSize: Dp = DefaultIconSize
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = contentDescription,
            tint = contentColor,
            modifier = Modifier.size(iconSize)
        )
    }
}

@get:StringRes
private val CalculatorError.messageRes: Int
    get() = when (this) {
        CalculatorError.DivisionByZero -> R.string.error_division_by_zero
        CalculatorError.InvalidInput -> R.string.error_invalid_input
    }

@Composable
private fun AutoSizeText(
    text: String,
    color: Color,
    maxFontSize: TextUnit,
    minFontSize: TextUnit,
    modifier: Modifier = Modifier,
    fontWeight: FontWeight = FontWeight.Normal,
    textAlign: TextAlign = TextAlign.End
) {
    var fontSize by remember(text, maxFontSize) { mutableStateOf(maxFontSize) }

    Text(
        text = text,
        color = color,
        fontSize = fontSize,
        fontWeight = fontWeight,
        textAlign = textAlign,
        maxLines = 1,
        softWrap = false,
        onTextLayout = { layoutResult ->
            if (layoutResult.didOverflowWidth && fontSize > minFontSize) {
                fontSize = (fontSize.value * SHRINK_FACTOR)
                    .coerceAtLeast(minFontSize.value)
                    .sp
            }
        },
        modifier = modifier
    )
}

@Preview(showBackground = true, backgroundColor = 0xFF000000)
@Composable
fun TestView() {
    val navController = rememberNavController()
    ShowCalculator(navController = navController)
}
