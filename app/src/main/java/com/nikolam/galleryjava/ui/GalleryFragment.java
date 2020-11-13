package com.nikolam.galleryjava.ui;

import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import com.nikolam.galleryjava.R;
import com.nikolam.galleryjava.data.loader.model.GalleryImage;
import com.nikolam.galleryjava.databinding.GalleryFragmentBinding;
import com.nikolam.galleryjava.ui.adapter.ImageAdapter;
import com.nikolam.galleryjava.ui.adapter.ImageClickListener;

import java.io.File;
import java.util.ArrayList;

import static androidx.core.content.FileProvider.getUriForFile;

public class GalleryFragment extends Fragment implements ImageClickListener{

    private GalleryViewModel mViewModel;

    private GalleryFragmentBinding binding;

    private ImageAdapter imageAdapter;

    private MenuItem deleteItem;

    private Toolbar toolbar;


    public static GalleryFragment newInstance() {
        return new GalleryFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        binding = DataBindingUtil.inflate(
                inflater, R.layout.gallery_fragment, container, false);

        imageAdapter = new ImageAdapter(this);

        setHasOptionsMenu (true);

        GridLayoutManager manager = new GridLayoutManager(this.getContext(), 4);

        binding.setAdapter(imageAdapter);
        binding.galleryGridRecycleView.setLayoutManager(manager);
        binding.setLifecycleOwner(this);

        View view = binding.getRoot();


        //toolbar
        toolbar = view.findViewById(R.id.gallery_toolbar);
        toolbar.getMenu().findItem(R.id.delete_selected).setEnabled(false);
        toolbar.getMenu().findItem(R.id.share).setEnabled(false);

        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this).get(GalleryViewModel.class);
        observeData();
    }


    public void observeData(){

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId() == R.id.delete_selected) {
                    mViewModel.deleteSelectedImages();
                    toolbar.getMenu().findItem(R.id.share).setEnabled(false);
                    toolbar.getMenu().findItem(R.id.delete_selected).setEnabled(false);
                    toolbar.getMenu().findItem(R.id.delete_selected).setTitle("Delete All");
                    imageAdapter.currentlyNotSelecting();
                } else if(item.getItemId() == R.id.share){
                    try
                    {
                        File myFile = new File(mViewModel.selectedImages.get(0).getmImageUrl());
                        MimeTypeMap mime = MimeTypeMap.getSingleton();
                        String ext = myFile.getName().substring(myFile.getName().lastIndexOf(".") + 1);
                        String type = mime.getMimeTypeFromExtension(ext);
                        Intent sharingIntent = new Intent("android.intent.action.SEND");
                        sharingIntent.setType(type);
                        Uri contentUri = mViewModel.selectedImages.get(0).getContentUri();
                        sharingIntent.putExtra("android.intent.extra.STREAM", contentUri);
                        startActivity(Intent.createChooser(sharingIntent, "Share using"));
                        Log.d("Images", contentUri.toString());
                    }
                    catch (Exception e)
                    {
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                        Log.d("Images", e.getLocalizedMessage());
                    }
                }

                return true;
            }
        });


        mViewModel.getAllImages().observe(getViewLifecycleOwner(), new Observer<ArrayList<GalleryImage>>() {
            @Override
            public void onChanged(ArrayList<GalleryImage> images) {
                imageAdapter.setImages(images);
            }

        });

    }

    @Override
    public void selectedImage(GalleryImage image) {
        if(mViewModel.selectedImages.isEmpty()){
            imageAdapter.currentlyIsSelecting();
            toolbar.getMenu().findItem(R.id.delete_selected).setEnabled(true);
            toolbar.getMenu().findItem(R.id.share).setEnabled(true);
        } else {
            toolbar.getMenu().findItem(R.id.share).setEnabled(false);
        }

        int size = mViewModel.selectedImages.size() + 1;
        toolbar.getMenu().findItem(R.id.delete_selected).setTitle("Delete All (" + size + ")");
        mViewModel.selectedImages.add(image);
    }

    @Override
    public void deselectedImage(GalleryImage image) {
        mViewModel.selectedImages.remove(image);

        if(mViewModel.selectedImages.isEmpty()){
            imageAdapter.currentlyNotSelecting();
            toolbar.getMenu().findItem(R.id.delete_selected).setEnabled(false);
            toolbar.getMenu().findItem(R.id.share).setEnabled(false);
        } else if(mViewModel.selectedImages.size() == 1){
            toolbar.getMenu().findItem(R.id.share).setEnabled(true);
        }

    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        Log.d("Images", "on prepare");
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mViewModel.clearData();
    }
}