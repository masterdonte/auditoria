INSERT INTO usuario (ativo, created_at, updated_at, email, nome, login) VALUES(true, now(), now(), 'jonathas.pdcase@emap.ma.gov.br', 'Jonathas Pimenta', 'jonathas.pdcase');
INSERT INTO usuario (ativo, created_at, updated_at, email, nome, login) VALUES(true, now(), now(), 'yandson.pdcase@emap.ma.gov.br', 'Yandson Saraiva', 'yandson.pdcase');
insert into perfil (nome) values ('ADMIN'), ('AUDITOR'), ('GESTOR'), ('AUDITADO') , ('DEFAULT');
INSERT INTO usuario_perfil (usuario_id, perfil_id) SELECT u.id, p.id FROM usuario u CROSS JOIN perfil p WHERE u.login = 'jonathas.pdcase' AND p.nome IN ('ADMIN', 'DEFAULT');
INSERT INTO usuario_perfil (usuario_id, perfil_id) SELECT u.id, p.id FROM usuario u CROSS JOIN perfil p WHERE u.login = 'yandson.pdcase' AND p.nome IN ('DEFAULT');
