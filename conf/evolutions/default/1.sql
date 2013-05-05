# Starting schema

# --- !Ups

create sequence "circle_pipe_id_seq";
create sequence "cutting_id_seq";
create sequence "cutting_plan_id_seq";
create sequence "extended_pipe_id_seq";
create sequence "extended_sheet_id_seq";
create sequence "material_id_seq";
create sequence "order_id_seq";
create sequence "pack_id_seq";
create sequence "part_id_seq";
create sequence "part_definition_id_seq";
create sequence "semiproduct_id_seq";
create sequence "shape_id_seq";
create sequence "sheet_id_seq";
create sequence "square_pipe_id_seq";
create sequence "order_def_seq";
create sequence "common_shape_id_seq";
create table "circle_pipe" ("id" int NOT NULL DEFAULT nextval('circle_pipe_id_seq') PRIMARY KEY,"common_shape_id" INTEGER NOT NULL,"thickness" DECIMAL(21,2) NULL,"radius" DECIMAL(21,2) NULL);
create table "cutting" ("id" int NOT NULL DEFAULT nextval('cutting_id_seq') PRIMARY KEY,"finish_time" TIMESTAMP,"semiproduct_id" INTEGER NOT NULL, "cutplan_id" INTEGER NOT NULL);
create table "cutting_plan" ("id" int NOT NULL DEFAULT nextval('cutting_plan_id_seq') PRIMARY KEY,"name" VARCHAR(254) NOT NULL,"file" BYTEA NOT NULL,"hidden" BOOLEAN NOT NULL,"filter" VARCHAR(254) NOT NULL);
create table "extended_pipe" ("id" int NOT NULL DEFAULT nextval('extended_pipe_id_seq') PRIMARY KEY,"common_shape_id" INTEGER NOT NULL,"length" DECIMAL(21,2) NULL);
create table "extended_sheet" ("id" int NOT NULL DEFAULT nextval('extended_sheet_id_seq') PRIMARY KEY,"common_shape_id" INTEGER NOT NULL,"width" DECIMAL(21,2) NULL,"height" DECIMAL(21,2) NULL);
create table "material" ("id" int NOT NULL DEFAULT nextval('material_id_seq') PRIMARY KEY,"name" VARCHAR(254) NOT NULL);
create table "order" ("id" int NOT NULL DEFAULT nextval('order_id_seq') PRIMARY KEY,"name" VARCHAR(254) NOT NULL,"filling_date" TIMESTAMP NOT NULL,"due_date" TIMESTAMP,"status" INTEGER NOT NULL);
create table "pack" ("id" int NOT NULL DEFAULT nextval('pack_id_seq') PRIMARY KEY,"material_id" INTEGER NOT NULL,"shape_id" INTEGER NOT NULL,"unlimited" BOOLEAN NOT NULL,"delivery_date" TIMESTAMP NOT NULL,"heat_no" VARCHAR(254) NOT NULL);
create table "part" ("id" int NOT NULL DEFAULT nextval('part_id_seq') PRIMARY KEY,"order_def_id" INTEGER,"part_def_id" INTEGER NOT NULL,"cutting_id" INTEGER NOT NULL,"damaged" BOOLEAN NOT NULL);
create table "part_definition" ("id" int NOT NULL DEFAULT nextval('part_definition_id_seq') PRIMARY KEY,"file" BYTEA NOT NULL,"filter" VARCHAR(254) NOT NULL,"name" VARCHAR(254) NOT NULL,"hidden" BOOLEAN NOT NULL);
create table "part_def_in_cut_plan" ("cut_plan_id" INTEGER NOT NULL,"part_def_id" INTEGER NOT NULL,"count" INTEGER NOT NULL);
alter table "part_def_in_cut_plan" add constraint "pk_part_def_in_cut_plan" primary key("cut_plan_id","part_def_id");
create table "order_def" ("id" int NOT NULL DEFAULT nextval('order_def_seq') PRIMARY KEY, "order_id" INTEGER NOT NULL,"part_def_id" INTEGER NOT NULL,"count" INTEGER NOT NULL,"filter" VARCHAR(254) NOT NULL);
create table "semiproduct" ("id" int NOT NULL DEFAULT nextval('semiproduct_id_seq') PRIMARY KEY, "pack_id" int NOT NULL, "serial_no" VARCHAR(254) NOT NULL);
create table "shape" ("id" int NOT NULL DEFAULT nextval('shape_id_seq') PRIMARY KEY, "basic_shape_id" INTEGER NOT NULL, "extended_shape_id" INTEGER NULL);
create table "sheet" ("id" int NOT NULL DEFAULT nextval('sheet_id_seq') PRIMARY KEY,"common_shape_id" INTEGER NOT NULL,"thickness" DECIMAL(21,2) NULL);
create table "square_pipe" ("id" int NOT NULL DEFAULT nextval('square_pipe_id_seq') PRIMARY KEY,"common_shape_id" INTEGER NOT NULL,"thickness" DECIMAL(21,2) NULL,"diameter" DECIMAL(21,2) NULL);
create table "common_shape" ("id" int NOT NULL DEFAULT nextval('common_shape_id_seq') PRIMARY KEY)
alter table "circle_pipe" add constraint "fk_circle_pipe_common_shape" foreign key("common_shape_id") references "common_shape"("id") on update NO ACTION on delete NO ACTION;
alter table "extended_pipe" add constraint "fk_extended_pipe_common_shape" foreign key("common_shape_id") references "common_shape"("id") on update NO ACTION on delete NO ACTION;
alter table "extended_sheet" add constraint "fk_extended_sheet_common_shape" foreign key("common_shape_id") references "common_shape"("id") on update NO ACTION on delete NO ACTION;
alter table "pack" add constraint "fk_pack_shape" foreign key("shape_id") references "shape"("id") on update NO ACTION on delete NO ACTION;
alter table "pack" add constraint "fk_pack_material" foreign key("material_id") references "material"("id") on update NO ACTION on delete NO ACTION;
alter table "part" add constraint "fk_part_order_def" foreign key("order_def_id") references "order_def"("id") on update NO ACTION on delete NO ACTION;
alter table "part" add constraint "fk_part_part_def" foreign key("part_def_id") references "part_definition"("id") on update NO ACTION on delete NO ACTION;
alter table "part" add constraint "fk_part_cutting" foreign key("cutting_id") references "cutting"("id") on update NO ACTION on delete NO ACTION;
alter table "part_def_in_cut_plan" add constraint "fk_part_def_in_cut_plan_part_def" foreign key("part_def_id") references "part_definition"("id") on update NO ACTION on delete NO ACTION;
alter table "part_def_in_cut_plan" add constraint "fk_part_def_in_cut_plan_cut_plan" foreign key("cut_plan_id") references "cutting_plan"("id") on update NO ACTION on delete NO ACTION;
alter table "order_def" add constraint "fk_order_def_order" foreign key("order_id") references "order"("id") on update NO ACTION on delete NO ACTION;
alter table "order_def" add constraint "fk_order_def_part_def" foreign key("part_def_id") references "part_definition"("id") on update NO ACTION on delete NO ACTION;
alter table "semiproduct" add constraint "fk_semiproduct_pack" foreign key("pack_id") references "pack"("id") on update NO ACTION on delete NO ACTION;
alter table "sheet" add constraint "fk_sheet_common_shape" foreign key("common_shape_id") references "common_shape"("id") on update NO ACTION on delete NO ACTION;
alter table "square_pipe" add constraint "fk_square_pipe_common_shape" foreign key("common_shape_id") references "common_shape"("id") on update NO ACTION on delete NO ACTION;
alter table "cutting" add constraint "fk_cutting_semiproduct" foreign key("semiproduct_id") references "semiproduct"("id") on update NO ACTION on delete NO ACTION;
alter table "cutting" add constraint "fk_cutting_cutting_plan" foreign key("cutplan_id") references "cutting_plan"("id") on update NO ACTION on delete NO ACTION;
alter table "shape" add constraint "fk_shape_basic_shape" foreign key("basic_shape_id") references "common_shape"("id") on update NO ACTION on delete NO ACTION;
alter table "shape" add constraint "fk_shape_extended_shape" foreign key("extended_shape_id") references "common_shape"("id") on update NO ACTION on delete NO ACTION;
# --- !Downs

alter table "circle_pipe" drop constraint "fk_circle_pipe_common_shape";
alter table "extended_pipe" drop constraint "fk_extended_pipe_common_shape";
alter table "extended_sheet" drop constraint "fk_extended_sheet_common_shape";
alter table "pack" drop constraint "fk_pack_shape";
alter table "pack" drop constraint "fk_pack_material";
alter table "part" drop constraint "fk_part_order";
alter table "part" drop constraint "fk_part_part_def";
alter table "part" drop constraint "fk_part_cutting";
alter table "part_def_in_cut_plan" drop constraint "fk_part_def_in_cut_plan_part_def";
alter table "part_def_in_cut_plan" drop constraint "fk_part_def_in_cut_plan_cut_plan";
alter table "order_def" drop constraint "fk_order_def_order";
alter table "order_def" drop constraint "fk_order_def_part_def";
alter table "semiproduct" drop constraint "fk_semiproduct_pack";
alter table "sheet" drop constraint "fk_sheet_common_shape";
alter table "square_pipe" drop constraint "fk_square_pipe_common_shape";
alter table "cutting" drop constraint "fk_cutting_semiproduct";
alter table "cutting" drop constraint "fk_cutting_cutting_plan";
alter table "shape" drop constraint "fk_shape_basic_shape";
alter table "shape" drop constraint "fk_shape_extended_shape";
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
drop table "order_def";
drop table "semiproduct";
drop table "shape";
drop table "sheet";
drop table "square_pipe";
drop table "common_shape";
drop sequence "circle_pipe_id_seq";
drop sequence "cutting_id_seq";
drop sequence "cutting_plan_id_seq";
drop sequence "extended_circle_pipe_id_seq";
drop sequence "extended_sheet_id_seq";
drop sequence "extended_square_pipe_id_seq";
drop sequence "material_id_seq";
drop sequence "order_id_seq";
drop sequence "pack_id_seq";
drop sequence "part_id_seq";
drop sequence "part_definition_id_seq";
drop sequence "semiproduct_id_seq";
drop sequence "shape_id_seq";
drop sequence "sheet_id_seq";
drop sequence "square_pipe_id_seq";
drop sequence "order_def_seq";
drop sequence "common_shape_id_seq";
