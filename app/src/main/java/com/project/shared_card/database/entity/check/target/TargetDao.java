package com.project.shared_card.database.entity.check.target;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.project.shared_card.database.entity.check.product.FullProduct;
import com.project.shared_card.database.entity.check.product.ProductEntity;

import java.util.List;

@Dao
public interface TargetDao {
    @Insert
    void add(TargetEntity target);
    @Query("select * from target where group_name_id = :id order by status")
    LiveData<List<FullTarget>> getAll(long id);
    @Update
    void update(TargetEntity entity);
}
