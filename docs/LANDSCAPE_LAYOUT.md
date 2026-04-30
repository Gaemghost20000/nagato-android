# Nagato Landscape Layout Design

## Current Status: Deferred

The hermes-agent webui handles all internal navigation (project list, chat, 
file browser, settings) within the WebView. In landscape mode, the WebView 
adapts naturally.

## Recommended Approach for v1

**Single WebView, full-width landscape.**
The webui already has responsive layouts. Over-engineering a native 3-pane 
layout would duplicate functionality the webui provides.

## Future Enhancement

When the native app replaces more WebView components with native RecyclerViews, 
the landscape layout would be:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <!-- Left Pane (1/3 width) — Project List -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/landscape_projects"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        app:layout_constraintWidth_percent="0.25"/>

    <!-- Center Pane (1/2 width) — Chat WebView -->
    <WebView
        android:id="@+id/landscape_chat"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        app:layout_constraintStart_toEndOf="@id/landscape_projects"
        app:layout_constraintWidth_percent="0.50"/>

    <!-- Right Pane (1/4 width) — Debug/Logs -->
    <androidx.recyclerview.widget.RecyclerView
        android:id="@+id/landscape_debug"
        android:layout_width="0dp"
        android:layout_height="match_parent"
        app:layout_constraintStart_toEndOf="@id/landscape_chat"/>

</androidx.constraintlayout.widget.ConstraintLayout>
```

> Would require native project list + native chat renderer — deferred to Phase 2.
