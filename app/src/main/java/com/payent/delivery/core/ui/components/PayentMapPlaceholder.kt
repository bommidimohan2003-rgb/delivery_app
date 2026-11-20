package com.payent.delivery.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun PayentMapPlaceholder(
    vendorName: String,
    vendorAddress: String,
    modifier: Modifier = Modifier,
    originLabel: String = "Partner Location",
    isPickupMap: Boolean = true,
    journeyStatusText: String = "READY FOR NAVIGATION",
    onDirectionsClick: () -> Unit = {}
) {
    PayentIllustrativeMapView(
        destinationName = vendorName,
        destinationAddress = vendorAddress,
        modifier = modifier,
        originLabel = originLabel,
        isPickupMap = isPickupMap,
        journeyStatusText = journeyStatusText,
        onDirectionsClick = onDirectionsClick
    )
}
