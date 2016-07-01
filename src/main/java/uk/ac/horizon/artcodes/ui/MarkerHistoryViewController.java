/*
 * Artcodes recognises a different marker scheme that allows the
 * creation of aesthetically pleasing, even beautiful, codes.
 * Copyright (C) 2013-2016  The University of Nottingham
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as published
 *     by the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package uk.ac.horizon.artcodes.ui;

import android.animation.Animator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.content.ContextCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import uk.ac.horizon.artcodes.R;
import uk.ac.horizon.artcodes.model.Action;
import uk.ac.horizon.artcodes.model.MarkerImage;

/**
 * This class controls a relative layout to create a display of the current markers that make up
 * the current detected action and any future action.
 */
public class MarkerHistoryViewController
{

    private static final int SEPARATOR_WIDTH_DP = 25;
    private static final int VIEW_WIDTH_DP = 50;
    private static final int IMAGE_WIDTH_DP = 45;
    private static final int BOTTOM_MARGIN_DP = 5;
    private static final int ANIMATION_DURATION_MS = 300;

    private final Context context;
    private final RelativeLayout relativeLayout;
    private Handler uiHandler;
    private List<MarkerImage> existingMarkerImages = new ArrayList<>();
    private Action existingAction = null;

    private final float displayDensity;

    public MarkerHistoryViewController(Context context, RelativeLayout relativeLayout, Handler uiHandler)
    {
        this.context = context;
        this.relativeLayout = relativeLayout;
        this.uiHandler = uiHandler;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        displayDensity = displayMetrics.density;
    }

    private Map<MarkerImage, View> displayedViews = new HashMap<>();
    private Map<Integer, View> missingViewsByPosition = new HashMap<>();
    private Map<Integer, View> seperatorViewsByPosition = new HashMap<>();

    public void update(List<MarkerImage> incomingMarkerImages, final Action currentOrFutureAction)
    {
        if (currentOrFutureAction == null || incomingMarkerImages == null || incomingMarkerImages.isEmpty())
        {
            this.existingMarkerImages.clear();
            this.existingAction = null;
            incomingMarkerImages = new ArrayList<>();
        }

        final List<MarkerImage> markerImages = incomingMarkerImages;
        final Action.Match existingMatchType = existingAction!=null ? existingAction.getMatch() : Action.Match.any;
        final Action.Match currentMatchType = currentOrFutureAction!=null ? currentOrFutureAction.getMatch() : Action.Match.any;

        final int n = markerImages.size();
        final int viewSizePX = (int) (VIEW_WIDTH_DP * displayDensity);
        final int separatorWidthPX= (int) (SEPARATOR_WIDTH_DP *displayDensity);
        final int startX = (int) (relativeLayout.getWidth() / 2 - (n * viewSizePX / 2f) - ((n-1) * separatorWidthPX / 2f));
        final int finalTranslationY = relativeLayout.getHeight() - viewSizePX - (int) (BOTTOM_MARGIN_DP * displayDensity);
        final int finalTranslationYForSeparator = finalTranslationY + (int)(12.5*displayDensity);

        this.uiHandler.post(new Runnable()
        {
            @Override
            public void run()
            {

                // animate removal of views not in markerImages (and unneeded separators/placeholders)
                // TODO: instead of creating/destroying separators/placeholders they should be reused.

                // Remove placeholders
                List<Map.Entry<Integer, View>> missingToRemove = new ArrayList<>();
                for (Map.Entry<Integer, View> entry : missingViewsByPosition.entrySet())
                {
                    if (entry.getKey() >= markerImages.size()-1)
                    {
                        missingToRemove.add(entry);
                    }
                }
                for (Map.Entry<Integer, View> entry : missingToRemove)
                {
                    final View view = entry.getValue();
                    animateRemoval(view);
                    missingViewsByPosition.remove(entry.getKey());
                }

                // Remove separators
                List<Map.Entry<Integer, View>> separatorsToRemove = new ArrayList<>();
                if (currentOrFutureAction!=null && existingMatchType==currentMatchType)
                {
                    for (Map.Entry<Integer, View> entry : seperatorViewsByPosition.entrySet())
                    {
                        if (entry.getKey() >= markerImages.size() - 2)
                        {
                            separatorsToRemove.add(entry);
                        }
                    }
                }
                else
                {
                    separatorsToRemove.addAll(seperatorViewsByPosition.entrySet());
                }
                for (Map.Entry<Integer, View> entry : separatorsToRemove)
                {
                    final View view = entry.getValue();
                    animateRemoval(view);
                    seperatorViewsByPosition.remove(entry.getKey());
                }

                // Remove marker images
                List<Map.Entry<MarkerImage, View>> toRemove = new ArrayList<>();
                for (Map.Entry<MarkerImage, View> entry : displayedViews.entrySet())
                {
                    if (!markerImages.contains(entry.getKey()))
                    {
                        toRemove.add(entry);
                    }
                }
                for (Map.Entry<MarkerImage, View> entry : toRemove)
                {
                    final View view = entry.getValue();
                    animateRemoval(view);
                    displayedViews.remove(entry.getKey());
                }

                // Add or re-position views of images in markerImages (and separators/placeholders)

                int count = 0;
                for (MarkerImage markerImage : markerImages)
                {

                    if (markerImage != null)
                    {
                        // add/re-position marker image

                        final View missingView = missingViewsByPosition.get(count);
                        if (missingView != null)
                        {
                            animateRemoval(missingView);
                            missingViewsByPosition.remove(count);
                        }

                        View markerImageView = displayedViews.get(markerImage);
                        if (markerImageView == null)
                        {
                            markerImageView = LayoutInflater.from(context).inflate(R.layout.marker_thumbnail, null);
                            final ImageView imageView = (ImageView) markerImageView.findViewById(R.id.marker_thumbnail_image);
                            if (imageView == null)
                            {
                                continue;
                            }
                            imageView.setImageBitmap(markerImage.image);
                        }

                        // Note: Translation ignores scale, and scale scales around the centre of the view.

                        int finalTranslationX = startX + count * (viewSizePX + separatorWidthPX);

                        if (markerImage.newDetection)
                        {
                            final int initialImageWidthPX = (int) (relativeLayout.getWidth() * markerImage.width);
                            final int initialImageHeightPX = (int) (relativeLayout.getHeight() * markerImage.height);

                            final float initialScale = (Math.max(initialImageWidthPX, initialImageHeightPX) / displayDensity) / IMAGE_WIDTH_DP;

                            final int initialImageXCenter = (int) (relativeLayout.getWidth() * markerImage.x + initialImageWidthPX / 2);
                            final int initialImageYCenter = (int) (relativeLayout.getHeight() * markerImage.y + initialImageHeightPX / 2);
                            final int initialViewX = initialImageXCenter - viewSizePX / 2;
                            final int initialViewY = initialImageYCenter - viewSizePX / 2;

                            markerImageView.setTranslationX(initialViewX);
                            markerImageView.setTranslationY(initialViewY);
                            markerImageView.setScaleX(initialScale);
                            markerImageView.setScaleY(initialScale);
                            markerImageView.setAlpha(0f);

                            if (!displayedViews.containsKey(markerImage))
                            {
                                relativeLayout.addView(markerImageView);
                                displayedViews.put(markerImage, markerImageView);
                            }
                            animateEnterOrMove(markerImageView, finalTranslationX, finalTranslationY, 1);
                        }
                        else
                        {
                            if (!displayedViews.containsKey(markerImage))
                            {
                                markerImageView.setScaleX(1);
                                markerImageView.setScaleY(1);
                                markerImageView.setTranslationX(finalTranslationX);
                                markerImageView.setTranslationY(finalTranslationY);
                                markerImageView.setAlpha(0f);
                                relativeLayout.addView(markerImageView);
                                displayedViews.put(markerImage, markerImageView);
                            }
                            animateEnterOrMove(markerImageView, finalTranslationX, finalTranslationY, 1);
                        }
                    }
                    else
                    {
                        // add/move placeholder for missing marker

                        View view = missingViewsByPosition.get(count);

                        final int finalTranslationX = startX + count * (viewSizePX + (int) (SEPARATOR_WIDTH_DP * displayDensity));

                        if (view == null)
                        {
                            view = createMarkerPlaceholder();
                            view.setTranslationX(finalTranslationX);
                            view.setTranslationY(finalTranslationY);
                            view.setAlpha(0f);
                            missingViewsByPosition.put(count, view);
                            relativeLayout.addView(view);
                        }

                        animateEnterOrMove(view, finalTranslationX, finalTranslationY);
                    }

                    // add/move separator
                    if (count < markerImages.size()-1)
                    {
                        View view = seperatorViewsByPosition.get(count);

                        final int finalTranslationX = startX + (count+1) * (viewSizePX + (int) (SEPARATOR_WIDTH_DP * displayDensity)) - (int)(SEPARATOR_WIDTH_DP *displayDensity);

                        if (view == null)
                        {
                            if (currentMatchType == Action.Match.all)
                            {
                                view = createGroupSeparator();
                            }
                            else
                            {
                                view = createSequenceSeparator();
                            }
                            view.setTranslationX(finalTranslationX);
                            view.setTranslationY(finalTranslationYForSeparator);
                            view.setAlpha(0f);
                            seperatorViewsByPosition.put(count, view);
                            relativeLayout.addView(view);
                        }

                        animateEnterOrMove(view, finalTranslationX, finalTranslationYForSeparator);
                    }

                    ++count;
                }
            }
        });


        this.existingMarkerImages = markerImages;
        this.existingAction = currentOrFutureAction;
    }

    private void animateEnterOrMove(View view, int x, int y)
    {
        view.animate()
                .alpha(1)
                .translationX(x)
                .translationY(y)
                .setDuration(ANIMATION_DURATION_MS)
                .setInterpolator(new LinearInterpolator())
                .start();
    }

    private void animateEnterOrMove(View view, int x, int y, float scale)
    {
        view.animate()
                .alpha(1)
                .translationX(x)
                .translationY(y)
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(ANIMATION_DURATION_MS)
                .setInterpolator(
                        new LinearInterpolator()
                ).start();
    }

    private void animateRemoval(final View view)
    {
        view.animate()
                .setListener(new Animator.AnimatorListener()
                {
                    @Override
                    public void onAnimationStart(Animator animator)
                    {

                    }

                    @Override
                    public void onAnimationEnd(Animator animator)
                    {
                        relativeLayout.removeView(view);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator)
                    {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animator)
                    {

                    }
                })
                .alpha(0)
                .setInterpolator(new LinearInterpolator())
                .setDuration(ANIMATION_DURATION_MS)
                .start();
    }

    private View createMarkerPlaceholder()
    {
        ImageView iv = new ImageView(context);
        Drawable d = ContextCompat.getDrawable(context, R.drawable.missing_marker_thumbnail_background);
        iv.setImageDrawable(d);
        return iv;
    }

    private View createGroupSeparator()
    {
        ImageView iv = new ImageView(context);
        Drawable d = ContextCompat.getDrawable(context, R.drawable.marker_thumbnail_separator_group_image);
        iv.setImageDrawable(d);
        return iv;
    }

    private View createSequenceSeparator()
    {
        ImageView iv = new ImageView(context);
        Drawable d = ContextCompat.getDrawable(context, R.drawable.marker_thumbnail_separator_sequence_image);
        iv.setImageDrawable(d);
        return iv;
    }

}
