package com.payent.delivery.feature.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountBalanceWallet
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payent.delivery.R

/**
 * Editorial Redesign of Screen 1: Welcome / Partner Portal
 * matching the reference design (Phone 1).
 */
@Composable
fun WelcomeScreen(
    onNavigateToRegister: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val scrollState = rememberScrollState()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AuthTokens.ScreenBg)
    ) {
        // Subtle pale-green decorative curved wave in bottom corner
        PaleGreenCurvedBackground(
            showTopRight = false,
            showBottomLeft = true
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .verticalScroll(scrollState),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // 1. Top Brand Header: Green 'p' logo + "payent" + "DELIVERY"
            PayentBrandHeader()

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Hero Motorcycle Delivery Illustration
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.partner_delivery_bike),
                    contentDescription = "Payent Delivery Partner Motorcycle",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(185.dp),
                    contentScale = ContentScale.Fit
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. Hero Titles & Supporting Description
            Text(
                text = "PAYENT DELIVERY",
                style = TextStyle(
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Black,
                    color = AuthTokens.TextPrimary,
                    letterSpacing = (-0.5).sp
                )
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Partner Portal",
                style = TextStyle(
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = AuthTokens.TextPrimary,
                    letterSpacing = (-0.3).sp
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Deliver rentals, electronics, and gear for local Payent vendors. Flexible hours and automatic weekly payouts.",
                style = TextStyle(
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    fontWeight = FontWeight.Normal,
                    color = AuthTokens.TextSecondary
                )
            )

            Spacer(modifier = Modifier.height(22.dp))

            // 4. Three-Column Benefits Section (Flexible Hours, Local Deliveries, Weekly Payouts)
            WelcomeBenefitsCard(
                item1Title = "Flexible\nHours",
                item1Icon = Icons.Outlined.Timer,
                item2Title = "Local\nDeliveries",
                item2Icon = Icons.Outlined.LocationOn,
                item3Title = "Weekly\nPayouts",
                item3Icon = Icons.Outlined.AccountBalanceWallet
            )

            Spacer(modifier = Modifier.height(24.dp))

            // 5. Primary Actions: Create Partner Account & Sign In to Existing Account
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                PrimaryBlackButton(
                    text = "Create Partner Account",
                    onClick = onNavigateToRegister
                )

                SecondaryOutlinedButton(
                    text = "Sign In to Existing Account",
                    onClick = onNavigateToLogin
                )
            }

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}
