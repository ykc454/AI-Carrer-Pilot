package com.example.aicareerpilot.presentation.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.aicareerpilot.presentation.viewmodel.NewsUiState
import com.example.aicareerpilot.presentation.viewmodel.NewsViewModel

@Composable
fun JobMarketScreen(
    viewModel: NewsViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsState()

    Scaffold(
        containerColor = Color.Black
    ) { padding ->

        when (state) {

            is NewsUiState.Loading -> {

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is NewsUiState.Success -> {

                val articles =
                    (state as NewsUiState.Success).news

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {


                    items(articles.take(100)) { article ->

                        var expanded by remember {
                            mutableStateOf(false)
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            onClick = {
                                expanded = !expanded
                            },
                            shape = RoundedCornerShape(20.dp),
                            elevation = CardDefaults.cardElevation(
                                defaultElevation = 8.dp
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = Color(0xFF444444)
                            ),
                            colors = CardDefaults.cardColors(
                                containerColor = Color.Black
                            )
                        ) {

                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp)
                            ) {

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {

                                    Text(
                                        text = article.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.titleMedium,
                                        modifier = Modifier.weight(1f)
                                    )

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Text(
                                        text = if (expanded) "−" else "+",
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleLarge
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                article.source?.name?.let {
                                    Text(
                                        text = it,
                                        color = Color.LightGray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }

                                AnimatedVisibility(visible = expanded) {

                                    Column {

                                        Spacer(modifier = Modifier.height(14.dp))

                                        HorizontalDivider(
                                            color = Color(0xFF444444),
                                            thickness = 1.dp
                                        )

                                        Spacer(modifier = Modifier.height(14.dp))

                                        Text(
                                            text = article.description ?: "No description available",
                                            color = Color(0xFFE0E0E0),
                                            style = MaterialTheme.typography.bodyMedium,
                                            lineHeight = MaterialTheme.typography.bodyMedium.lineHeight
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            is NewsUiState.Error -> {

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {

                    Text(
                        text = "Failed to load job market news",
                        color = Color.Red
                    )
                }
            }
        }
    }
}
