package com.wisp.finance.ui.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.wisp.finance.R

private const val GITHUB_URL = "https://github.com/WispApps/Wisp-Finance"
private const val GITLAB_URL = "https://gitlab.com/wispapps/wisp-finance"

@Composable
fun PrivacyScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.privacy_title),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onBackground,
            )
        }

        PrivacyCard(
            title = stringResource(R.string.privacy_local_title),
            body = stringResource(R.string.sec_scope),
        )
        PrivacyCard(
            title = stringResource(R.string.privacy_no_account_title),
            body = stringResource(R.string.privacy_no_account_body),
        )
        PrivacyCard(
            title = stringResource(R.string.privacy_insights_title),
            body = stringResource(R.string.privacy_insights_body),
        )
        PrivacyCard(
            title = stringResource(R.string.privacy_currency_title),
            body = stringResource(R.string.privacy_currency_body),
        )

        PrivacyCard(title = stringResource(R.string.sec_nextcloud), body = stringResource(R.string.sec_cloud_intro))

        Text(
            text = stringResource(R.string.privacy_source_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 22.dp, bottom = 8.dp),
        )
        PrivacyLinkCard(
            title = stringResource(R.string.privacy_github),
            url = GITHUB_URL,
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITHUB_URL)))
            },
        )
        PrivacyLinkCard(
            title = stringResource(R.string.privacy_gitlab),
            url = GITLAB_URL,
            onClick = {
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(GITLAB_URL)))
            },
        )
    }
}

@Composable
private fun PrivacyCard(title: String, body: String) {
    Card(modifier = Modifier.fillMaxWidth().padding(top = 10.dp)) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 5.dp),
            )
        }
    }
}

@Composable
private fun PrivacyLinkCard(title: String, url: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp)
            .clickable(onClick = onClick),
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = url,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 5.dp),
            )
        }
    }
}
