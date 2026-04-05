package com.musclefit.app.ui.muscle

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.res.stringResource
import coil.compose.AsyncImage
import com.musclefit.app.R

class MuscleExercisesActivity : ComponentActivity() {
    companion object {
        const val EXTRA_MUSCLE_KEY = "muscle_key"
        const val EXTRA_BODY_SIDE = "body_side"
    }

    private val viewModel: MuscleExercisesViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val muscleKey = intent.getStringExtra(EXTRA_MUSCLE_KEY)
        val bodySide = intent.getStringExtra(EXTRA_BODY_SIDE).orEmpty().ifEmpty { "front" }

        setContent {
            val state by viewModel.state.observeAsState(MuscleExercisesUiState.loading())

            LaunchedEffect(muscleKey, bodySide) {
                viewModel.load(muscleKey, bodySide)
            }

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MuscleExercisesScreen(
                        state = state,
                        onBack = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
private fun MuscleExercisesScreen(
    state: MuscleExercisesUiState,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text(text = stringResource(R.string.cd_back))
            }
            Text(
                text = if (state.error == null && state.areaName.isNotBlank()) {
                    stringResource(R.string.muscle_exercises_title, state.areaName)
                } else {
                    stringResource(R.string.muscle_exercises_title, stringResource(R.string.home_body_map_default_label))
                },
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 4.dp)
            )
        }

        val subtitle = if (state.error == null && !state.loading) {
            stringResource(
                R.string.muscle_exercises_subtitle,
                if (state.bodyView == "back") stringResource(R.string.body_map_side_back) else stringResource(R.string.body_map_side_front),
                state.exercises.size
            )
        } else {
            ""
        }

        if (subtitle.isNotBlank()) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
        }

        when {
            state.loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Text(
                            text = stringResource(R.string.muscle_exercises_loading),
                            modifier = Modifier.padding(top = 10.dp),
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            state.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.muscle_exercises_error),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(state.exercises, key = { it.id }) { item ->
                        MuscleExerciseCard(item = item)
                    }
                }
            }
        }
    }
}

@Composable
private fun MuscleExerciseCard(item: MuscleExerciseItem) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = RoundedCornerShape(18.dp),
        tonalElevation = 1.dp,
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            AsyncImage(
                model = resolveImageData(item.image),
                contentDescription = stringResource(R.string.detail_tutorial_image),
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(188.dp)
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(14.dp))
            )

            Text(
                text = item.name,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 10.dp)
            )

            Text(
                text = item.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp)
            )

            if (item.tips.isNotEmpty()) {
                Text(
                    text = stringResource(R.string.muscle_exercise_tips_title),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(top = 8.dp)
                )
                Text(
                    text = item.tips.joinToString(separator = "\n") { "• $it" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

private fun resolveImageData(imageName: String?): Any {
    var value = imageName.orEmpty().trim()
    if (value.startsWith("http://") || value.startsWith("https://")) {
        return value
    }
    if (value.startsWith("@drawable/")) {
        value = value.removePrefix("@drawable/")
    }
    if (value.endsWith(".png")) {
        value = value.removeSuffix(".png")
    }
    if (value.isNotEmpty()) {
        DRAWABLE_ID_BY_NAME[value]?.let { return it }
    }
    return R.drawable.ex_fullbody_01
}

private val DRAWABLE_ID_BY_NAME = mapOf(
    "ex_back_01" to R.drawable.ex_back_01,
    "ex_back_triceps_01" to R.drawable.ex_back_triceps_01,
    "ex_chest_01" to R.drawable.ex_chest_01,
    "ex_chest_02" to R.drawable.ex_chest_02,
    "ex_chest_03" to R.drawable.ex_chest_03,
    "ex_core_01" to R.drawable.ex_core_01,
    "ex_core_02" to R.drawable.ex_core_02,
    "ex_fullbody_01" to R.drawable.ex_fullbody_01,
    "ex_fullbody_02" to R.drawable.ex_fullbody_02,
    "ex_legs_calf_01" to R.drawable.ex_legs_calf_01,
    "ex_legs_thigh_01" to R.drawable.ex_legs_thigh_01,
    "ex_shoulder_01" to R.drawable.ex_shoulder_01,
    "ex_triceps_01" to R.drawable.ex_triceps_01,
    "mimg_calf" to R.drawable.mimg_calf,
    "mimg_thigh" to R.drawable.mimg_thigh
)
