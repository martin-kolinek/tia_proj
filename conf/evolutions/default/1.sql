# Starting schema

# --- !Ups

create table "circle_pipe" ("id" INTEGER NOT NULL PRIMARY KEY,"shape_id" INTEGER NOT NULL,"thickness" DOUBLE PRECISION NOT NULL,"radius" DOUBLE PRECISION NOT NULL);
create table "cutting" ("id" INTEGER NOT NULL PRIMARY KEY,"finish_time" DATE,"semiproduct_id" INTEGER NOT NULL);
create table "cutting_plan" ("id" INTEGER NOT NULL PRIMARY KEY,"name" VARCHAR(254) NOT NULL,"file" BYTEA NOT NULL,"hidden" BOOLEAN NOT NULL,"filter" VARCHAR(254) NOT NULL);
create table "extended_circle_pipe" ("id" INTEGER NOT NULL PRIMARY KEY,"circle_pipe_id" INTEGER NOT NULL,"length" DOUBLE PRECISION NOT NULL);
create table "extended_sheet" ("id" INTEGER NOT NULL PRIMARY KEY,"sheet_id" INTEGER NOT NULL,"width" DOUBLE PRECISION NOT NULL,"height" DOUBLE PRECISION NOT NULL);
create table "extended_square_pipe" ("id" INTEGER NOT NULL PRIMARY KEY,"square_pipe_id" INTEGER NOT NULL,"length" DOUBLE PRECISION NOT NULL);
create table "material" ("id" INTEGER NOT NULL PRIMARY KEY,"name" VARCHAR(254) NOT NULL);
create table "order" ("id" INTEGER NOT NULL PRIMARY KEY,"name" VARCHAR(254) NOT NULL,"filling_date" DATE NOT NULL,"due_date" DATE,"status" INTEGER NOT NULL);
create table "pack" ("id" INTEGER NOT NULL PRIMARY KEY,"material_id" INTEGER NOT NULL,"shape_id" INTEGER NOT NULL,"unlimited" BOOLEAN NOT NULL,"delivery_date" DATE NOT NULL,"heat_no" VARCHAR(254) NOT NULL);
create table "part" ("id" INTEGER NOT NULL PRIMARY KEY,"order_id" INTEGER,"part_def_id" INTEGER NOT NULL,"cut_plan_id" INTEGER NOT NULL,"damaged" BOOLEAN NOT NULL);
create table "part_definition" ("id" INTEGER NOT NULL PRIMARY KEY,"file" BYTEA NOT NULL,"filter" VARCHAR(254) NOT NULL,"name" VARCHAR(254) NOT NULL,"hidden" BOOLEAN NOT NULL);
create table "part_def_in_cut_plan" ("cut_plan_id" INTEGER NOT NULL,"part_def_id" INTEGER NOT NULL,"count" INTEGER NOT NULL);
alter table "part_def_in_cut_plan" add constraint "pk_part_def_in_cut_plan" primary key("cut_plan_id","part_def_id");
create table "part_def_in_order" ("order_id" INTEGER NOT NULL,"part_def_id" INTEGER NOT NULL,"count" INTEGER NOT NULL,"filter" VARCHAR(254) NOT NULL);
alter table "part_def_in_order" add constraint "pk_part_def_in_order" primary key("order_id","part_def_id");
create table "semiproduct" ("id" INTEGER NOT NULL PRIMARY KEY,"serial_no" VARCHAR(254) NOT NULL);
create table "shape" ("id" INTEGER NOT NULL PRIMARY KEY,"text_description" VARCHAR(254) NOT NULL);
create table "sheet" ("id" INTEGER NOT NULL PRIMARY KEY,"shape_id" INTEGER NOT NULL,"thickness" DOUBLE PRECISION NOT NULL);
create table "square_pipe" ("id" INTEGER NOT NULL PRIMARY KEY,"shape_id" INTEGER NOT NULL,"thickness" DOUBLE PRECISION NOT NULL,"diameter" DOUBLE PRECISION NOT NULL);
alter table "circle_pipe" add constraint "fk_circle_pipe_shape" foreign key("shape_id") references "sheet"("id") on update NO ACTION on delete NO ACTION;
alter table "extended_circle_pipe" add constraint "fk_extended_circle_pipe_circle_pipe" foreign key("circle_pipe_id") references "sheet"("id") on update NO ACTION on delete NO ACTION;
alter table "extended_sheet" add constraint "fk_extended_sheet_sheet" foreign key("sheet_id") references "sheet"("id") on update NO ACTION on delete NO ACTION;
alter table "extended_square_pipe" add constraint "fk_extended_square_pipe_square_pipe" foreign key("square_pipe_id") references "sheet"("id") on update NO ACTION on delete NO ACTION;
alter table "pack" add constraint "fk_pack_shape" foreign key("shape_id") references "shape"("id") on update NO ACTION on delete NO ACTION;
alter table "pack" add constraint "fk_pack_material" foreign key("material_id") references "material"("id") on update NO ACTION on delete NO ACTION;
alter table "part" add constraint "fk_part_order" foreign key("order_id") references "order"("id") on update NO ACTION on delete NO ACTION;
alter table "part" add constraint "fk_part_part_def" foreign key("part_def_id") references "part_definition"("id") on update NO ACTION on delete NO ACTION;
alter table "part" add constraint "fk_part_cut_plan" foreign key("cut_plan_id") references "cutting_plan"("id") on update NO ACTION on delete NO ACTION;
alter table "part_def_in_cut_plan" add constraint "fk_part_def_in_cut_plan_part_def" foreign key("part_def_id") references "part_definition"("id") on update NO ACTION on delete NO ACTION;
alter table "part_def_in_cut_plan" add constraint "fk_part_def_in_cut_plan_cut_plan" foreign key("cut_plan_id") references "cutting_plan"("id") on update NO ACTION on delete NO ACTION;
alter table "part_def_in_order" add constraint "fk_part_def_in_order_order" foreign key("order_id") references "order"("id") on update NO ACTION on delete NO ACTION;
alter table "part_def_in_order" add constraint "fk_part_def_in_order_part_def" foreign key("part_def_id") references "part_definition"("id") on update NO ACTION on delete NO ACTION;
alter table "semiproduct" add constraint "fk_semiproduct_pack" foreign key("id") references "pack"("id") on update NO ACTION on delete NO ACTION;
alter table "sheet" add constraint "fk_sheet_shape" foreign key("shape_id") references "sheet"("id") on update NO ACTION on delete NO ACTION;
alter table "square_pipe" add constraint "fk_squre_pipe_shape" foreign key("shape_id") references "sheet"("id") on update NO ACTION on delete NO ACTION;

# --- !Downs

alter table "circle_pipe" drop constraint "fk_circle_pipe_shape";
alter table "extended_circle_pipe" drop constraint "fk_extended_circle_pipe_circle_pipe";
alter table "extended_sheet" drop constraint "fk_extended_sheet_sheet";
alter table "extended_square_pipe" drop constraint "fk_extended_square_pipe_square_pipe";
alter table "pack" drop constraint "fk_pack_shape";
alter table "pack" drop constraint "fk_pack_material";
alter table "part" drop constraint "fk_part_order";
alter table "part" drop constraint "fk_part_part_def";
alter table "part" drop constraint "fk_part_cut_plan";
alter table "part_def_in_cut_plan" drop constraint "fk_part_def_in_cut_plan_part_def";
alter table "part_def_in_cut_plan" drop constraint "fk_part_def_in_cut_plan_cut_plan";
alter table "part_def_in_order" drop constraint "fk_part_def_in_order_order";
alter table "part_def_in_order" drop constraint "fk_part_def_in_order_part_def";
alter table "semiproduct" drop constraint "fk_semiproduct_pack";
alter table "sheet" drop constraint "fk_sheet_shape";
alter table "square_pipe" drop constraint "fk_square_pipe_shape";
drop table "circle_pipe";
drop table "cutting";
drop table "cutting_plan";
drop table "extended_circle_pipe";
drop table "extended_sheet";
drop table "extended_square_pipe";
drop table "material";
drop table "order";
drop table "pack";
drop table "part";
drop table "part_definition";
alter table "part_def_in_cut_plan" drop constraint "pk_part_def_in_cut_plan";
drop table "part_def_in_cut_plan";
alter table "part_def_in_order" drop constraint "pk_part_def_in_order";
drop table "part_def_in_order";
drop table "semiproduct";
drop table "shape";
drop table "sheet";
drop table "square_pipe";

