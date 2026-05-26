import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.aicareerpilot.presentation.screens.Screen
import com.example.aicareerpilot.presentation.viewmodel.AuthUiState
import com.example.aicareerpilot.presentation.viewmodel.AuthViewModel
import com.example.aicareerpilot.R


@Composable
fun SignInScreen(
    navController: NavHostController
) {

    val authViewModel: AuthViewModel = hiltViewModel()

    val context = LocalContext.current

    val uiState by authViewModel.uiState.collectAsState()

    LaunchedEffect(uiState) {

        when (uiState) {

            is AuthUiState.Success -> {

                Toast.makeText(
                    context,
                    "Google Login Success",
                    Toast.LENGTH_SHORT
                ).show()

                navController.navigate(Screen.Home.route) {

                    popUpTo(Screen.SignIn.route) {
                        inclusive = true
                    }
                }

                authViewModel.resetState()
            }

            is AuthUiState.Error -> {

                Toast.makeText(
                    context,
                    (uiState as AuthUiState.Error).message,
                    Toast.LENGTH_SHORT
                ).show()

                authViewModel.resetState()
            }

            else -> Unit
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {

        when (uiState) {

            is AuthUiState.Loading -> {

                CircularProgressIndicator()
            }

            else -> {

                OutlinedButton(

                    onClick = {
                        authViewModel.signInWithGoogle(context)
                    },

                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),

                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color.Black

                    )

                ) {

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {

                        Image(
                            painter = painterResource(
                                R.drawable.google_logo_new
                            ),
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(
                            modifier = Modifier.width(15.dp)
                        )

                        Text(
                            text = "Continue with Google",
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}