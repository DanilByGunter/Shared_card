package com.project.shared_card.activity.database.entity.check.target;

import androidx.room.Embedded;
import androidx.room.Relation;

import com.project.shared_card.activity.database.entity.currency.CurrencyEntity;
import com.project.shared_card.activity.database.entity.shop.target.ShopTargetEntity;
import com.project.shared_card.activity.database.entity.categories.target.CategoriesTargetEntity;
import com.project.shared_card.activity.database.entity.user_name.UserNameEntity;

public class FullTarget {
    @Embedded
    public TargetEntity target;
    @Relation(
            parentColumn = "currency_id",
            entityColumn = "id")
    public CurrencyEntity currency;
    @Relation(
            parentColumn = "category_id",
            entityColumn = "id")
    public CategoriesTargetEntity category;
    @Relation(
            parentColumn = "shop_id",
            entityColumn = "id")
    public ShopTargetEntity shop;
    @Relation(
            parentColumn = "user_name_creator_id",
            entityColumn = "id")
    public UserNameEntity creator;
    @Relation(
            parentColumn = "user_name_buyer_id",
            entityColumn = "id")
    public UserNameEntity buyer;

}
