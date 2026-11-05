package com.example.calculadora

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun ShowHistory(
    viewModel: CalculatorViewModel,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.calculator_background))
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        HistoryHeader(onBackClick = { navController.popBackStack() })
        if (viewModel.contentResult.isEmpty()) {
            EmptyHistory(modifier = Modifier.weight(1f).fillMaxWidth())
        } else {
            HistoryList(
                history = viewModel.contentResult,
                modifier = Modifier.weight(1f).fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HistoryHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        CalculatorIconButton(
            iconRes = R.drawable.ic_arrow_back,
            contentDescription = stringResource(R.string.content_description_back),
            backgroundColor = colorResource(R.color.calculator_button_digit),
            contentColor = colorResource(R.color.calculator_text_primary),
            onClick = onBackClick,
            modifier = Modifier.size(44.dp),
            iconSize = 22.dp
        )
        Text(
            text = stringResource(R.string.history_title),
            color = colorResource(R.color.calculator_text_primary),
            fontSize = 28.sp
        )
    }
}

@Composable
private fun EmptyHistory(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.history_empty),
            color = colorResource(R.color.calculator_text_secondary),
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun HistoryList(history: List<HistoryEntry>, modifier: Modifier = Modifier) {
    LazyColumn(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        items(history) { entry ->
            Text(
                text = stringResource(
                    R.string.history_entry,
                    entry.leftOperand,
                    entry.operation,
                    entry.rightOperand,
                    entry.result
                ),
                color = colorResource(R.color.calculator_text_primary),
                fontSize = 24.sp,
                textAlign = TextAlign.End,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
