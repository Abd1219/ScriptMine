package com.abdapps.scriptmine.ui.theme

import androidx.compose.ui.graphics.Color

// Futuristic Dark Theme Colors
val FuturisticBackground = Color(0xFF0A0A0F) // Deep space black
val FuturisticSurface = Color(0xFF1A1A2E) // Dark blue-gray
val FuturisticSurfaceVariant = Color(0xFF16213E) // Slightly lighter surface

// Neon accent colors
val NeonCyan = Color(0xFF00F5FF) // Bright cyan
val NeonPurple = Color(0xFF8A2BE2) // Electric purple
val NeonGreen = Color(0xFF39FF14) // Bright green
val NeonPink = Color(0xFFFF1493) // Hot pink
val NeonBlue = Color(0xFF0080FF) // Electric blue

// Primary gradient colors (Cyan to Purple)
val PrimaryGradientStart = NeonCyan
val PrimaryGradientEnd = NeonPurple
val Primary = NeonCyan
val PrimaryVariant = NeonPurple

// Secondary gradient (Green to Blue)
val SecondaryGradientStart = NeonGreen
val SecondaryGradientEnd = NeonBlue

// Text colors
val TextPrimary = Color(0xFFE0E6ED) // Light gray-blue
val TextSecondary = Color(0xFF9CA3AF) // Medium gray
val TextPlaceholder = Color(0xFF6B7280) // Darker gray
val TextAccent = NeonCyan

// Surface variations
val SurfaceElevated = Color(0xFF252545) // Elevated surface
val SurfaceCard = Color(0xFF1E1E3F) // Card background
val SurfaceInput = Color(0xFF2A2A4A) // Input field background

// Border and glow colors
val BorderGlow = Color(0x80_00F5FF) // Cyan glow with transparency
val BorderSecondary = Color(0x40_8A2BE2) // Purple glow with transparency
val BorderSuccess = Color(0x80_39FF14) // Green glow
val BorderError = Color(0x80_FF1493) // Pink glow

// Glass effect colors
val GlassOverlay = Color(0x20FFFFFF) // White overlay for glass effect
val GlassStroke = Color(0x40FFFFFF) // White stroke for glass borders

// Status colors
val SuccessColor = NeonGreen
val ErrorColor = NeonPink
val WarningColor = Color(0xFFFFAA00) // Orange
val InfoColor = NeonBlue