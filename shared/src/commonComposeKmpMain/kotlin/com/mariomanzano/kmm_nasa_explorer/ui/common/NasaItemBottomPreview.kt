package com.mariomanzano.kmm_nasa_explorer.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.ParagraphStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextIndent
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mariomanzano.kmm_nasa_explorer.domain.NasaItem
import com.mariomanzano.kmm_nasa_explorer.utils.stringResource

@Composable
fun <T : NasaItem> NasaItemBottomPreview(item: T?, onGoToDetail: (T) -> Unit) {
    if (item != null) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = item.date,
                textAlign = TextAlign.Center
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = item.title ?: "",
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center
            )
            Text(
                text = buildAnnotatedString {
                    append(
                        AnnotatedString(
                            text = item.description ?: "",
                            paragraphStyle = ParagraphStyle(
                                textIndent = TextIndent(
                                    firstLine = 25.sp,
                                    restLine = 10.sp
                                )
                            )
                        )
                    )
                },
                maxLines = 10,
                overflow = TextOverflow.Ellipsis
            )
            Button(
                onClick = { onGoToDetail(item) },
                modifier = Modifier.align(Alignment.CenterHorizontally)
            ) {
                Text(text = stringResource("go_to_detail"))
            }
        }
    } else {
        // It needs something for when the item is null, or the Bottom Sheet will crash in this case
        Spacer(modifier = Modifier.height(1.dp))
    }
}