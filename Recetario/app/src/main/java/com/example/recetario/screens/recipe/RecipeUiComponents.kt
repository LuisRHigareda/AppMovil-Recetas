package com.example.recetario.screens.recipe

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.recetario.screens.auth.RecetarioLightGray

@Composable
fun RecipeImagePreview(
    imageUri: String?,
    modifier: Modifier = Modifier,
    placeholderText: String = "Sin fotografía"
) {
    val context = LocalContext.current

    val drawableResId = remember(imageUri) {
        if (imageUri?.startsWith("drawable:") == true) {
            val resourceName = imageUri.removePrefix("drawable:")

            context.resources.getIdentifier(
                resourceName,
                "drawable",
                context.packageName
            )
        } else {
            0
        }
    }

    val imageBitmap = remember(imageUri) {
        if (imageUri.isNullOrBlank() || imageUri.startsWith("drawable:")) {
            null
        } else {
            runCatching {
                context.contentResolver.openInputStream(Uri.parse(imageUri)).use { inputStream ->
                    inputStream?.let {
                        BitmapFactory.decodeStream(it)?.asImageBitmap()
                    }
                }
            }.getOrNull()
        }
    }

    when {
        drawableResId != 0 -> {
            Image(
                painter = painterResource(id = drawableResId),
                contentDescription = "Fotografía de la receta",
                contentScale = ContentScale.Crop,
                modifier = modifier.clip(RoundedCornerShape(14.dp))
            )
        }

        imageBitmap != null -> {
            Image(
                bitmap = imageBitmap,
                contentDescription = "Fotografía de la receta",
                contentScale = ContentScale.Crop,
                modifier = modifier.clip(RoundedCornerShape(14.dp))
            )
        }

        else -> {
            Box(
                modifier = modifier
                    .clip(RoundedCornerShape(14.dp))
                    .background(RecetarioLightGray),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = placeholderText,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun RecipeTagRow(
    tags: List<String>,
    modifier: Modifier = Modifier
) {
    if (tags.isEmpty()) return

    Row(
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        modifier = modifier.horizontalScroll(rememberScrollState())
    ) {
        tags.forEach { tag ->
            Text(
                text = "#$tag",
                color = Color.Black,
                modifier = Modifier
                    .border(
                        width = 1.dp,
                        color = Color.LightGray,
                        shape = RoundedCornerShape(50)
                    )
                    .background(
                        color = Color.White,
                        shape = RoundedCornerShape(50)
                    )
                    .padding(horizontal = 8.dp, vertical = 3.dp)
            )
        }
    }
}