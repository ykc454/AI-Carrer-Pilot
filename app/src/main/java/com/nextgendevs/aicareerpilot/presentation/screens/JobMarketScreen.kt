import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.nextgendevs.aicareerpilot.presentation.viewmodel.DiscussionUiState
import com.nextgendevs.aicareerpilot.presentation.viewmodel.DiscussionViewModel
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import com.nextgendevs.aicareerpilot.presentation.viewmodel.ResumeViewModel

@Composable
fun DeveloperTrendsScreen(resumeViewModel: ResumeViewModel,
    viewModel: DiscussionViewModel = hiltViewModel()
) {

    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    when (state) {

        is DiscussionUiState.Loading -> {

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is DiscussionUiState.Error -> {

            Box(
                modifier = Modifier.fillMaxSize().padding(20.dp),
                contentAlignment = Alignment.Center
            ) {

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Text(
                        text = "📶",
                        style = MaterialTheme.typography.displayLarge
                    )

                    Spacer(
                        modifier = Modifier.height(12.dp)
                    )

                    Text(
                        (state as DiscussionUiState.Error).message,
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        is DiscussionUiState.Success -> {

            val questions =
                (state as DiscussionUiState.Success)
                    .questions
                    .sortedByDescending { it.viewCount }
                    .take(50)

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {

                items(questions) { question ->

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),

                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(question.link)
                            )
                            context.startActivity(intent)
                        },

                        shape = RoundedCornerShape(20.dp),

                        colors = CardDefaults.cardColors(
                            containerColor = Color.Black
                        ),

                        border = BorderStroke(
                            1.dp,
                            Color.White.copy(alpha = 0.3f)
                        ),

                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 2.dp
                        )
                    ) {

                        Column(
                            modifier = Modifier.padding(20.dp)
                        ) {

                            Text(
                                text = question.title,
                                color = Color.White,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 24.sp,
                                maxLines = 2
                            )

                            Spacer(
                                modifier = Modifier.height(16.dp)
                            )

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(16.dp)
                            ) {

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.04f)
                                ) {
                                    Text(
                                        text = "${question.viewCount} Views",
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 6.dp
                                        ),
                                        color = Color(0xFFA1A1AA),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.04f)
                                ) {
                                    Text(
                                        text = "${question.score} Score",
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 6.dp
                                        ),
                                        color = Color(0xFFA1A1AA),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }

                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = Color.White.copy(alpha = 0.04f)
                                ) {
                                    Text(
                                        text = "${question.answerCount} Answers",
                                        modifier = Modifier.padding(
                                            horizontal = 10.dp,
                                            vertical = 6.dp
                                        ),
                                        color = Color(0xFFA1A1AA),
                                        style = MaterialTheme.typography.labelMedium
                                    )
                                }
                            }

                            Spacer(
                                modifier = Modifier.height(16.dp)
                            )

                            FlowRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                question.tags.take(4).forEach { tag ->

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = Color.White.copy(alpha = 0.03f)
                                    ) {
                                        Text(
                                            text = tag,
                                            modifier = Modifier.padding(
                                                horizontal = 10.dp,
                                                vertical = 5.dp
                                            ),
                                            color = Color(0xFFD4D4D8),
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }

                            Spacer(
                                modifier = Modifier.height(18.dp)
                            )

                            HorizontalDivider(
                                color = Color.White.copy(alpha = 0.05f)
                            )

                            Spacer(
                                modifier = Modifier.height(14.dp)
                            )

                            Text(
                                text = "View Discussion",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}